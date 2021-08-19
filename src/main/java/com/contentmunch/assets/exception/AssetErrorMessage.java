package com.contentmunch.assets.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetErrorMessage {
    private String message;

}
