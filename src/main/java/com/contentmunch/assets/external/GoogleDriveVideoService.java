package com.contentmunch.assets.external;

import com.contentmunch.assets.configuration.AssetDriveConfig;
import com.contentmunch.assets.data.video.VideoMetadata;
import com.contentmunch.assets.data.video.VideoUploadMetadata;
import com.contentmunch.assets.exception.AssetException;
import com.contentmunch.assets.exception.AssetUnauthorizedException;
import com.contentmunch.assets.exception.VideoUploadException;
import com.contentmunch.assets.service.VideoService;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.client.json.gson.GsonFactory.getDefaultInstance;

@Service
@Slf4j
public class GoogleDriveVideoService implements VideoService {


    private final Credential credential;
    private final HttpRequestFactory requestFactory;
    private static final String UPLOAD_URL_FORMAT = "https://www.googleapis.com/upload/drive/v3/files%s";


    public GoogleDriveVideoService(AssetDriveConfig assetDriveConfig) {
        TokenResponse response = new TokenResponse();
        response.setRefreshToken(assetDriveConfig.getRefreshToken());
        try {
            this.credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(newTrustedTransport())
                    .setJsonFactory(getDefaultInstance())
                    .setTokenServerUrl(new GenericUrl(assetDriveConfig.getTokenServer()))
                    .setClientAuthentication(new BasicAuthentication(
                            assetDriveConfig.getClientId(), assetDriveConfig.getClientSecret()))
                    .build()
                    .setFromTokenResponse(response);
            this.requestFactory = credential.getTransport().createRequestFactory();

        } catch (GeneralSecurityException e) {
            log.error("Security Exception", e);
            throw new AssetUnauthorizedException(e.getMessage());
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    @Override
    public VideoUploadMetadata initiateVideoUpload(String folderId, String name, String description, String mimeType) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setParents(List.of(folderId));
            fileMetadata.setMimeType(mimeType);
            fileMetadata.setDescription(description);

            HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(String.format(UPLOAD_URL_FORMAT, "?uploadType=resumable")),
                    new JsonHttpContent(getDefaultInstance(), fileMetadata));

            request.getHeaders().setAuthorization("Bearer " + getAccessToken());
            request.getHeaders().set("X-Upload-Content-Type", mimeType);
            request.getHeaders().set("X-Upload-Content-Length", "*");

            HttpResponse response = request.execute();
            String resumableUploadUrl = response.getHeaders().getLocation();
            log.info("Resumable upload session initiated: {}", resumableUploadUrl);
            var uploadId = extractUploadId(resumableUploadUrl);

            return uploadId
                    .map(id -> new VideoUploadMetadata(uploadId.get(), resumableUploadUrl))
                    .orElseThrow(() -> new AssetException("Failure to initiate upload: No upload Id present"));
        } catch (IOException | RuntimeException e) {
            log.error("Error initiating video upload", e);
            throw new AssetException("Error initiating video upload");
        }
    }

    @Override
    @Retryable(
            retryFor = {VideoUploadException.class}, // Retry for specific exceptions
            maxAttempts = 5, // Max number of retries
            backoff = @Backoff(delay = 1000, multiplier = 2) // Exponential backoff (1s, 2s, 4s, etc.)
    )
    public Optional<VideoMetadata> uploadVideo(String uploadId, byte[] chunk, long startByte, long endByte, long totalSize, boolean isLastChunk) {
        try {
            String uploadUrl = String.format(UPLOAD_URL_FORMAT, "?upload_id=" + uploadId);
            HttpRequest request = requestFactory.buildPutRequest(new GenericUrl(uploadUrl),
                    new ByteArrayContent("application/octet-stream", chunk));

            // Set Content-Range header
            String contentRange = String.format("bytes %d-%d/%d", startByte, endByte - 1, totalSize);
            request.getHeaders().set("Content-Range", contentRange);
            log.info("Uploading chunk {}-{} of {}", startByte, endByte - 1, totalSize);

            HttpResponse response = request.execute();

            // Handle 308 Resume Incomplete error
            if (response.getStatusCode() == 308) {
                log.info("Chunk {}-{} uploaded successfully, server expects more chunks.", startByte, endByte - 1);
                return Optional.empty(); // Continue with next chunk
            }

            // Handle successful upload (last chunk)
            if (isLastChunk) {
                File uploadedFile = response.parseAs(File.class);
                log.info("Upload complete for file: {}", uploadedFile.getName());
                return Optional.of(buildVideoMetadata(uploadedFile));
            }

            return Optional.empty();
        } catch (HttpResponseException e) {
            String message = String.format("Error uploading video: %s for chunk %d-%d", e.getMessage(), startByte, endByte - 1);

            return switch (e.getStatusCode()) {
                case 308 -> {
                    // Handle 308 Resume Incomplete error separately
                    log.info("Upload still in progress, continue with next chunk: {}-{}", startByte, endByte - 1);
                    yield Optional.empty(); // Continue uploading the next chunk
                }
                case 500 -> {
                    log.error("Received 500 Internal Server Error. Retrying chunk {}-{}", startByte, endByte - 1, e);
                    throw new VideoUploadException(message); // Retry after 500 error
                }
                default -> {
                    log.error(message, e);
                    throw new AssetException(message); // For other HttpResponseException cases
                }
            };
        } catch (IOException | RuntimeException e) {
            // Handle all other IO/RuntimeExceptions
            String message = String.format("Unexpected error uploading video for chunk %d-%d", startByte, endByte - 1);
            log.error(message, e);
            throw new AssetException(message);
        }
    }

    @Override
    public Optional<VideoMetadata> getMetadata(String id) {
        try {
            String videoMetadataUrl = String.format(UPLOAD_URL_FORMAT, "/" + id + "?fields=id,name,description,parents,mimeType,webContentLink");
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(videoMetadataUrl));

            request.getHeaders().setAuthorization("Bearer " + getAccessToken());

            HttpResponse response = request.execute();
            File file = response.parseAs(File.class);
            return Optional.of(buildVideoMetadata(file));
        } catch (IOException | RuntimeException e) {
            log.error("Error getting video for id {}", id, e);
            return Optional.empty();
        }
    }

    private VideoMetadata buildVideoMetadata(File file) {
        return VideoMetadata.builder()
                .id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .mimeType(file.getMimeType())
                .folderId(file.getParents() != null && !file.getParents().isEmpty() ? file.getParents().get(0) : null)
                .videoUrl(file.getWebContentLink())
                .thumbnailUrl(file.getThumbnailLink())

                .build();
    }

    private Optional<String> extractUploadId(String resumableUploadUrl) {
        Pattern pattern = Pattern.compile("upload_id=([^&]+)");
        Matcher matcher = pattern.matcher(resumableUploadUrl);
        if (matcher.find()) {
            return Optional.of(matcher.group(1)); // Returns the value of upload_id
        }
        return Optional.empty(); // Return null if n
    }

    private String getAccessToken() throws IOException {
        if (credential.refreshToken()) { // Ensures token is always fresh
            return credential.getAccessToken();
        } else {
            throw new AssetUnauthorizedException("Failed to refresh access token");
        }
    }
}
