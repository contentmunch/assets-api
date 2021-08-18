package com.contentmunch.assets.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Assets {
    private List<Asset> assets;
    private String nextPageToken;
}
