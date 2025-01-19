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
        String largeAsset = null;
        if (file.getThumbnailLink() != null) {
            var thumbnailLinkContent = file.getThumbnailLink().split("=");
            if (thumbnailLinkContent.length > 0) {
                largeAsset = thumbnailLinkContent[0];
            }
        }

        return DriveAsset.builder().id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .folderId(folderId)
                .mimeType(file.getMimeType())
                .height(file.getImageMediaMetadata().getHeight())
                .width(file.getImageMediaMetadata().getWidth())
                .thumbnailAsset(file.getThumbnailLink())
                .smallAsset(largeAsset != null ? largeAsset + "=s600" : file.getWebContentLink())
                .mediumAsset(largeAsset != null ? largeAsset + "=s1200" : file.getWebContentLink())
                .largeAsset(largeAsset != null ? largeAsset : file.getWebContentLink())
                .originalAsset(file.getWebContentLink()).build();
    }

    public static DriveAsset from(File file) {
        return from(file, !file.getParents().isEmpty() ? file.getParents().get(0) : "");
    }

}
