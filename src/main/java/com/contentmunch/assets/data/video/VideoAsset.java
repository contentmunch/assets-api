package com.contentmunch.assets.data.video;

import lombok.Builder;

@Builder
public record VideoAsset(String id, String name, String description, String folderId, String mimeType, String videoUrl, String thumbnailUrl) {

}
