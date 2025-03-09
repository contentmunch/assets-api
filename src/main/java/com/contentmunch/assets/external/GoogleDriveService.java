package com.contentmunch.assets.external;

import com.contentmunch.assets.configuration.AssetDriveConfig;
import com.contentmunch.assets.data.drive.DriveAsset;
import com.contentmunch.assets.data.drive.DriveAssets;
import com.contentmunch.assets.data.drive.DriveFolder;
import com.contentmunch.assets.exception.AssetException;
import com.contentmunch.assets.exception.AssetUnauthorizedException;
import com.contentmunch.assets.utils.LocalFileUtils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.contentmunch.assets.utils.LocalFileUtils.from;
import static com.contentmunch.assets.utils.LocalFileUtils.stripExtension;
import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.client.json.gson.GsonFactory.getDefaultInstance;

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
    public Optional<DriveAsset> get(String folderId, String name) {
        try {
            Drive.Files.List list = drive.files().list()
                    .setQ("'" + folderId + "' in parents and name='" + name + "'")
                    .setPageSize(1)
                    .setFields(" files(" + IMAGE_FIELDS + ")");
            FileList result = list.execute();
            if (result == null || result.isEmpty())
                return Optional.empty();

            var driveAssets = DriveAssets
                    .builder()
                    .driveAssets(result.getFiles().stream().filter(file -> file.getMimeType().contains("image")).map(file -> DriveAsset.from(file, folderId))
                            .collect(Collectors.toList()))
                    .nextPageToken(result.getNextPageToken())
                    .build();
            if (driveAssets.getDriveAssets() == null || driveAssets.getDriveAssets().isEmpty())
                return Optional.empty();

            return Optional.of(driveAssets.getDriveAssets().get(0));
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

    public Optional<DriveAsset> get(String assetId) {
        try {
            File file = drive.files().get(assetId).setFields(IMAGE_FIELDS).execute();
            log.debug("Getting drive asset for assetId: {}", assetId);
            if (file.getMimeType().contains("image"))
                return Optional.of(DriveAsset.from(file));
            else
                return Optional.empty();
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
                fileMetadata.setDescription(description.orElseGet(() -> stripExtension(multipartFile.get().getOriginalFilename())));
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
        return get(folderId, name).orElseGet(() ->
                handleFileOperation(() -> {
                    File fileMetadata = createFileMetadata(name, description, folderId);
                    FileContent mediaContent = new FileContent(imageType, from(url, imageType));
                    return drive.files().create(fileMetadata, mediaContent)
                            .setFields(IMAGE_FIELDS)
                            .setSupportsAllDrives(true)
                            .execute();
                }, name, folderId)
        );
    }

    public DriveAsset createFrom(String fileId, String folderId, String name) {
        return get(folderId, name).orElseGet(() ->
                handleFileOperation(() -> {
                    File fileMetadata = createFileMetadata(name, Optional.empty(), folderId);
                    return drive.files().copy(fileId, fileMetadata)
                            .setFields(IMAGE_FIELDS)
                            .setSupportsAllDrives(true)
                            .execute();
                }, name, folderId)
        );
    }

    public DriveAsset create(String folderId, MultipartFile multipartFile, String name, Optional<String> description) {
        return get(folderId, name).orElseGet(() ->
                handleFileOperation(() -> {
                    File fileMetadata = createFileMetadata(name, description.or(() -> Optional.of(stripExtension(multipartFile.getOriginalFilename()))), folderId);
                    FileContent mediaContent = new FileContent(multipartFile.getContentType(), from(multipartFile));
                    return drive.files().create(fileMetadata, mediaContent)
                            .setFields(IMAGE_FIELDS)
                            .setSupportsAllDrives(true)
                            .execute();
                }, name, folderId)
        );
    }



    private DriveAsset handleFileOperation(Callable<File> fileOperation, String name, String folderId) {
        try {
            File file = fileOperation.call();
            log.debug("File {} created in folder {}", name, folderId);
            return DriveAsset.from(file, folderId);
        } catch (IOException e) {
            log.error("IO Exception", e);
            throw new AssetException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw new AssetException(e.getMessage());
        }
    }

    private File createFileMetadata(String name, Optional<String> description, String folderId) {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        description.ifPresent(fileMetadata::setDescription);
        fileMetadata.setParents(List.of(folderId));
        return fileMetadata;
    }
}
