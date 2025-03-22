package com.contentmunch.assets.service;

import com.contentmunch.assets.data.video.VideoAsset;
import com.contentmunch.assets.data.video.VideoAssets;
import com.contentmunch.assets.data.video.VideoUploadMetadata;

import java.util.Optional;

public interface VideoService {

    VideoUploadMetadata initiateVideoUpload(String folderId, String name, String description, String mimeType);

    @Deprecated
    Optional<VideoAsset> uploadVideo(String uploadId, byte[] chunk, long startByte, long endByte, long totalSize, boolean isLastChunk);

    Optional<VideoAsset> getVideo(String id);

    default VideoAssets findVideosByFolderId(String folderId) {
        return findVideosByFolderId(folderId, 10, null);
    }

    VideoAssets findVideosByFolderId(String folderId, Integer pageSize, String pageToken);

    void deleteVideo(String id);
}
