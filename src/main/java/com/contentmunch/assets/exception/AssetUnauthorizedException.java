package com.contentmunch.assets.exception;

public class AssetUnauthorizedException extends RuntimeException {
    public AssetUnauthorizedException(String message) {
        super(message);
    }
}
