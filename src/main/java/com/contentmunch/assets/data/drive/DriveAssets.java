package com.contentmunch.assets.data.drive;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DriveAssets {
    private List<DriveAsset> driveAssets;
    private String nextPageToken;
}
