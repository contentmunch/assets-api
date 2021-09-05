package com.contentmunch.assets.data.drive;

import com.google.api.services.drive.model.File;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveAsset {
    private String id;
    private String name;
    private String mimeType;
    private String description;
    private String folderId;
    private Integer height;
    private Integer width;
    private String thumbnailAsset;
    private String smallAsset;
    private String mediumAsset;
    private String largeAsset;
    private String originalAsset;

    public static DriveAsset from(File file, String folderId) {
        String largeAsset = file.getThumbnailLink().split("=")[0];
        return DriveAsset.builder().id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .folderId(folderId)
                .mimeType(file.getMimeType())
                .height(file.getImageMediaMetadata().getHeight())
                .width(file.getImageMediaMetadata().getWidth())
                .thumbnailAsset(file.getThumbnailLink())
                .smallAsset(largeAsset + "=s600")
                .mediumAsset(largeAsset + "=s1200")
                .largeAsset(largeAsset)
                .originalAsset(file.getWebContentLink()).build();
    }

    public static DriveAsset from(File file) {
        String largeAsset = file.getThumbnailLink().split("=")[0];
        return DriveAsset.builder().id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .folderId(!file.getParents().isEmpty() ? file.getParents().get(0) : "")
                .mimeType(file.getMimeType())
                .height(file.getImageMediaMetadata().getHeight())
                .width(file.getImageMediaMetadata().getWidth())
                .thumbnailAsset(file.getThumbnailLink())
                .smallAsset(largeAsset + "=s600")
                .mediumAsset(largeAsset + "=s1200")
                .largeAsset(largeAsset)
                .originalAsset(file.getWebContentLink()).build();
    }

}
