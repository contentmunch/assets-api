package com.contentmunch.assets.service;

import com.contentmunch.assets.configuration.AssetDriveConfig;
import com.contentmunch.assets.data.drive.DriveAsset;
import com.contentmunch.assets.data.drive.DriveAssets;
import com.contentmunch.assets.data.drive.DriveFolder;
import com.contentmunch.assets.exception.AssetException;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.exception.AssetUnauthorizedException;
import com.contentmunch.assets.utils.LocalFileUtils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.contentmunch.assets.utils.LocalFileUtils.*;
import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance;

@Service
@Slf4j
public class GoogleDriveService {

    private static final String IMAGE_FIELDS = "id, name, description, mimeType, parents, imageMediaMetadata, thumbnailLink, webContentLink";
    private final Drive drive;

    public GoogleDriveService(AssetDriveConfig assetDriveConfig) {
        TokenResponse response = new TokenResponse();
        response.setRefreshToken(assetDriveConfig.getRefreshToken());
        try {
            this.drive = new Drive.Builder(newTrustedTransport(), getDefaultInstance(),
                    new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                            newTrustedTransport())
                            .setJsonFactory(getDefaultInstance())
                            .setTokenServerUrl(
                                    new GenericUrl(assetDriveConfig.getTokenServer()))
                            .setClientAuthentication(new BasicAuthentication(
                                    assetDriveConfig.getClientId(), assetDriveConfig.getClientSecret()))
                            .build()
                            .setFromTokenResponse(response))
                    .setApplicationName(assetDriveConfig.getApplicationName())
                    .build();
        } catch (GeneralSecurityException e) {
            log.error("Security Exception", e);
            throw new AssetUnauthorizedException(e.getMessage());
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public DriveAssets list(String folderId, int pageSize, Optional<String> pageToken) {

        try {
            log.debug("Listing drive: {} with pageSize: {} and pageToken {}", folderId, pageSize, pageToken);
            Drive.Files.List list = drive.files().list()
                    .setQ("'" + folderId + "' in parents")
                    .setPageSize(pageSize)
                    .setFields("nextPageToken, files(" + IMAGE_FIELDS + ")");
            pageToken.ifPresent(list::setPageToken);
            FileList result = list.execute();


            return DriveAssets
                    .builder()
                    .driveAssets(result.getFiles().stream().filter(file -> file.getMimeType().contains("image")).map(file -> DriveAsset.from(file, folderId))
                            .collect(Collectors.toList()))
                    .nextPageToken(result.getNextPageToken())
                    .build();

        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public DriveAsset get(String assetId) {
        try {
            File file = drive.files().get(assetId).setFields(IMAGE_FIELDS).execute();
            log.debug("Getting drive asset for assetId: {}", assetId);
            if (file.getMimeType().contains("image"))
                return DriveAsset.from(file);
            else
                throw new AssetNotFoundException("Asset with assetId: " + assetId + " not found/ or is not an image");
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public DriveFolder createDrive(String folderId, String name) {
        try {
            var driveFolder = getDriveByName(folderId, name);
            if (driveFolder.isPresent()) {
                log.debug("Drive folder not created, folder with folderId: {} and name: {} exits!", folderId, name);
                return driveFolder.get();
            }

            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setParents(Collections.singletonList(folderId));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = drive.files().create(fileMetadata)
                    .setFields("id,name")
                    .execute();
            log.debug("Drive folder with folderId: {} and name: {} created successfully!", folderId, name);
            return DriveFolder.builder()
                    .id(file.getId())
                    .name(file.getName())
                    .build();

        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public DriveFolder getDrive(String folderId) {
        try {
            File file = drive.files().get(folderId)
                    .setFields("id,name")
                    .execute();
            return DriveFolder.builder()
                    .id(file.getId())
                    .name(file.getName())
                    .build();

        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public Optional<DriveFolder> getDriveByName(String folderId, String name) {
        try {
            FileList result = drive.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + name + "' and '" + folderId + "' in parents")
                    .setFields("files(id,name)")
                    .execute();
            if (result.getFiles().isEmpty())
                return Optional.empty();

            return Optional.of(DriveFolder.builder()
                    .id(result.getFiles().get(0).getId())
                    .name(result.getFiles().get(0).getName())
                    .build());

        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public void delete(String assetId) {
        try {
            drive.files().delete(assetId).execute();
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public byte[] getFile(String fileId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Drive.Files.Get file = drive.files().get(fileId);
            file.executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    public DriveAsset update(String id, Optional<MultipartFile> multipartFile, Optional<String> name, Optional<String> description) {
        try {
            File fileMetadata = new File();
            name.ifPresent(fileMetadata::setName);
            description.ifPresent(fileMetadata::setDescription);
            if (multipartFile.isPresent()) {
                FileContent mediaContent = new FileContent(multipartFile.get().getContentType(), from(multipartFile.get()));
                return DriveAsset.from(drive.files().update(id, fileMetadata, mediaContent)
                        .setFields(IMAGE_FIELDS)
                        .setSupportsAllDrives(true)
                        .execute());
            }

            return DriveAsset.from(drive.files().update(id, fileMetadata)
                    .setFields(IMAGE_FIELDS)
                    .setSupportsAllDrives(true)
                    .execute());
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        } finally {
            multipartFile.ifPresent(LocalFileUtils::deleteTempFile);
        }
    }

    public DriveAsset create(String folderId, String url, String imageType, String name, Optional<String> description) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            var imageFile = from(url);
            fileMetadata.setDescription(description.orElseGet(() -> stripExtension(imageFile.getName())));
            fileMetadata.setParents(List.of(folderId));
            FileContent mediaContent = new FileContent(imageType, imageFile);

            File file = drive.files().create(fileMetadata, mediaContent)
                    .setFields(IMAGE_FIELDS)
                    .setSupportsAllDrives(true)
                    .execute();
            return DriveAsset.from(file);

        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        } finally {
            deleteUrlFile();
        }
    }

    public DriveAsset create(String folderId, MultipartFile multipartFile, String name, Optional<String> description) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setDescription(description.orElseGet(() -> stripExtension(multipartFile.getOriginalFilename())));
            fileMetadata.setParents(List.of(folderId));
            FileContent mediaContent = new FileContent(multipartFile.getContentType(), from(multipartFile));

            File file = drive.files().create(fileMetadata, mediaContent)
                    .setFields(IMAGE_FIELDS)
                    .setSupportsAllDrives(true)
                    .execute();
            return DriveAsset.from(file, folderId);
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        } finally {
            deleteTempFile(multipartFile);
        }
    }
}
