package com.contentmunch.assets.external;

import com.contentmunch.assets.configuration.AssetDriveConfig;
import com.contentmunch.assets.data.video.VideoMetadata;
import com.contentmunch.assets.data.video.VideoUploadMetadata;
import com.contentmunch.assets.exception.AssetException;
import com.contentmunch.assets.exception.AssetUnauthorizedException;
import com.contentmunch.assets.service.VideoService;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

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
                    .map(VideoUploadMetadata::new)
                    .orElseThrow(() -> new AssetException("Failure to initiate upload: No upload Id present"));
        } catch (IOException | RuntimeException e) {
            log.error("Error initiating video upload", e);
            throw new AssetException("Error initiating video upload");
        }
    }

    @Override
    public Optional<VideoMetadata> uploadVideo(String uploadId, byte[] chunk, long startByte, long endByte, long totalSize, boolean isLastChunk) {
        try {
            String uploadUrl = String.format(UPLOAD_URL_FORMAT, "?upload_id=" + uploadId);
            HttpRequest request = requestFactory.buildPutRequest(new GenericUrl(uploadUrl),
                    new ByteArrayContent("application/octet-stream", chunk));

            request.getHeaders().set("Content-Range", String.format("bytes %d-%d/%d", startByte, endByte, totalSize));

            HttpResponse response = request.execute();
            log.info("Uploaded chunk: {}-{} of {}", startByte, endByte, totalSize);

            if (isLastChunk) {
                File uploadedFile = response.parseAs(File.class);
                return Optional.of(buildVideoMetadata(uploadedFile));
            }

            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            log.error("Error uploading video", e);
            throw new AssetException("Error uploading video");
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
        var uploadSplit = resumableUploadUrl.split("=");
        return uploadSplit.length > 1 ? Optional.of(uploadSplit[1]) : Optional.empty();
    }

    private String getAccessToken() throws IOException {
        if (credential.refreshToken()) { // Ensures token is always fresh
            return credential.getAccessToken();
        } else {
            throw new AssetUnauthorizedException("Failed to refresh access token");
        }
    }
}
