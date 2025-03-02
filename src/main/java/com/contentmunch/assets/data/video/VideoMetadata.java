package com.contentmunch.assets.data.video;

import lombok.Builder;

@Builder
public record VideoMetadata(String id, String name, String description, String folderId, String mimeType, String videoUrl, String thumbnailUrl) {

}
