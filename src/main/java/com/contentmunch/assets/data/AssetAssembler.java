package com.contentmunch.assets.data;

import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.data.drive.DriveAsset;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class AssetAssembler extends RepresentationModelAssemblerSupport<DriveAsset, Asset> {
    public AssetAssembler() {
        super(AssetsController.class, Asset.class);
    }

    @Override
    public Asset toModel(DriveAsset driveAsset) {
        Asset asset = createModelWithId(driveAsset.getId(), driveAsset);
        asset.setId(driveAsset.getId());
        asset.setName(driveAsset.getName());
        asset.setDescription(driveAsset.getDescription());
        asset.setFolderId(driveAsset.getFolderId());
        asset.setHeight(driveAsset.getHeight());
        asset.setWidth(driveAsset.getWidth());
        asset.add(Link.of(driveAsset.getThumbnailAsset(), "thumbnailAsset"));
        asset.add(Link.of(driveAsset.getSmallAsset(), "smallAsset"));
        asset.add(Link.of(driveAsset.getMediumAsset(), "mediumAsset"));
        asset.add(Link.of(driveAsset.getLargeAsset(), "largeAsset"));
        asset.add(Link.of(driveAsset.getOriginalAsset(), "originalAsset"));
        return asset;
    }
}
