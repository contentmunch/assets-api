package com.contentmunch.assets.service;

import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.data.AssetAssembler;
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

    public Asset get(String id) {
        return assetAssembler.toModel(googleDriveService.get(id));
    }

    public Asset create(String folderId, MultipartFile multipartFile, String name, Optional<String> description) {
        return assetAssembler.toModel(googleDriveService.create(folderId, multipartFile, name, description));
    }

    public Asset update(String folderId, MultipartFile multipartFile, String id, String name, Optional<String> description) {
        return assetAssembler.toModel(googleDriveService.update(folderId, multipartFile, id, name, description));
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
