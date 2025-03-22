package com.contentmunch.assets.data.video;

import lombok.Builder;

import java.util.List;

@Builder
public record VideoAssets(List<VideoAsset> videoAssets, String nextPageToken) {
}
