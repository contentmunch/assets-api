package com.contentmunch.assets.data;

import com.google.api.services.drive.model.File;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Asset {
    private String id;
    private String name;
    private String description;
    private String folderId;
    private Integer height;
    private Integer width;
    private String thumbnailAsset;
    private String smallAsset;
    private String mediumAsset;
    private String largeAsset;
    private String originalAsset;

    public static Asset from(File file, String folderId) {
        String largeAsset = file.getThumbnailLink().split("=")[0];
        return Asset.builder().id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .folderId(folderId)
                .height(file.getImageMediaMetadata().getHeight())
                .width(file.getImageMediaMetadata().getWidth())
                .thumbnailAsset(file.getThumbnailLink())
                .smallAsset(largeAsset + "=s600")
                .mediumAsset(largeAsset + "=s1200")
                .largeAsset(largeAsset)
                .originalAsset(file.getWebContentLink()).build();
    }

}
