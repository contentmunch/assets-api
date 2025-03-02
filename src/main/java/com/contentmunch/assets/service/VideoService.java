package com.contentmunch.assets.service;

import com.contentmunch.assets.data.video.VideoMetadata;
import com.contentmunch.assets.data.video.VideoUploadMetadata;
import com.contentmunch.assets.exception.AssetException;

import java.util.Optional;

public interface VideoService {

    VideoUploadMetadata initiateVideoUpload(String folderId, String name, String description, String mimeType);

    Optional<VideoMetadata> uploadVideo(String uploadId, byte[] chunk, long startByte, long endByte, long totalSize, boolean isLastChunk);

    Optional<VideoMetadata> getMetadata(String id);
}
