package com.contentmunch.assets.data.drive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveFolder {
    private String id;
    private String name;
}
