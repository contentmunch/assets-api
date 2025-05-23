package com.contentmunch.assets.service;

import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.data.AssetFolder;
import com.contentmunch.assets.data.assembler.AssetAssembler;
import com.contentmunch.assets.data.assembler.AssetFolderAssembler;
import com.contentmunch.assets.data.drive.DriveAssets;
import com.contentmunch.assets.external.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final GoogleDriveService googleDriveService;

    private final AssetAssembler assetAssembler;

    private final AssetFolderAssembler assetFolderAssembler;

    public Optional<Asset> get(String id) {
        var driveAsset = googleDriveService.get(id);
        return driveAsset.map(assetAssembler::toModel);
    }

    public Optional<Asset> getFromUrl(String url) {
        var parameters = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
        var id = parameters.getFirst("id");
        if (id != null)
            return get(id);
        else
            return Optional.empty();
    }

    public Asset create(String folderId, MultipartFile multipartFile, String name, Optional<String> description) {

        return assetAssembler.toModel(googleDriveService.create(folderId, multipartFile, name, description));
    }

    public Asset create(String folderId, String url, String imageType, String name, Optional<String> description) {

        return assetAssembler.toModel(googleDriveService.create(folderId, url, imageType, name, description));
    }

    public Asset createFrom(String fileId, String folderId, String name) {
        return assetAssembler.toModel(googleDriveService.createFrom(fileId, folderId, name));
    }

    public Asset update(String id, Optional<MultipartFile> multipartFile, Optional<String> name, Optional<String> description) {
        return assetAssembler.toModel(googleDriveService.update(id, multipartFile, name, description));
    }

    public AssetFolder createFolder(String folderId, String name) {
        return assetFolderAssembler.toModel(googleDriveService.createDrive(folderId, name));
    }

    public AssetFolder getFolder(String folderId) {
        return assetFolderAssembler.toModel(googleDriveService.getDrive(folderId));
    }

    public Optional<AssetFolder> getFolder(String folderId, String name) {
        var driveFolder = googleDriveService.getDriveByName(folderId, name);
        return driveFolder.map(assetFolderAssembler::toModel);
    }

    public void delete(String id) {
        googleDriveService.delete(id);
    }

    public CollectionModel<Asset> list(String folderId, int pageSize, Optional<String> nextPageToken) {
        DriveAssets driveAssets = googleDriveService.list(folderId, pageSize, nextPageToken);
        CollectionModel<Asset> model = CollectionModel.of(driveAssets.getDriveAssets().stream()
                .map(assetAssembler::toModel).collect(Collectors.toList()));

        model.add(linkTo(methodOn(AssetsController.class).list(folderId, pageSize, nextPageToken.orElse(null))).withSelfRel());
        if (driveAssets.getNextPageToken() != null)
            model.add(linkTo(methodOn(AssetsController.class).list(folderId, pageSize, driveAssets.getNextPageToken())).withRel("next"));

        return model;
    }

    public byte[] getFile(String fileId) {
        return googleDriveService.getFile(fileId);
    }
}
