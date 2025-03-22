package com.contentmunch.assets.service;

import com.contentmunch.assets.data.video.VideoAsset;
import com.contentmunch.assets.data.video.VideoAssets;
import com.contentmunch.assets.data.video.VideoUploadMetadata;

import java.util.List;
import java.util.Optional;

public interface VideoService {

    VideoUploadMetadata initiateVideoUpload(String folderId, String name, String description, String mimeType);

    @Deprecated
    Optional<VideoAsset> uploadVideo(String uploadId, byte[] chunk, long startByte, long endByte, long totalSize, boolean isLastChunk);

    Optional<VideoAsset> getMetadata(String id);

    default VideoAssets findByFolderId(String folderId) {
        return findByFolderId(folderId, 10, null);
    }

    VideoAssets findByFolderId(String folderId, Integer pageSize, String pageToken);
}
