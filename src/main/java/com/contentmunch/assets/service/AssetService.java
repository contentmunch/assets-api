package com.contentmunch.assets.service;

import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.data.AssetFolder;
import com.contentmunch.assets.data.assembler.AssetAssembler;
import com.contentmunch.assets.data.assembler.AssetFolderAssembler;
import com.contentmunch.assets.data.drive.DriveAssets;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Asset get(String id) {
        return assetAssembler.toModel(googleDriveService.get(id));
    }

    public Asset create(String folderId, MultipartFile multipartFile, String name, Optional<String> description) {

        return assetAssembler.toModel(googleDriveService.create(folderId, multipartFile, name, description));
    }

    public Asset update(String id, MultipartFile multipartFile, String name, Optional<String> description) {
        return assetAssembler.toModel(googleDriveService.update(multipartFile, id, name, description));
    }

    public AssetFolder createFolder(String folderId, String name) {
        return assetFolderAssembler.toModel(googleDriveService.createDrive(folderId, name));
    }

    public AssetFolder getFolder(String folderId) {
        return assetFolderAssembler.toModel(googleDriveService.getDrive(folderId));
    }

    public Optional<AssetFolder> getFolder(String folderId, String name) {
        var driveFolder = googleDriveService.getDriveByName(folderId, name);
        return driveFolder.isEmpty() ? Optional.empty() : Optional.of(assetFolderAssembler.toModel(driveFolder.get()));
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

}
