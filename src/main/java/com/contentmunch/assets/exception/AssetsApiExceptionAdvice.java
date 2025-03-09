package com.contentmunch.assets.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AssetsApiExceptionAdvice {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;


    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    @ResponseBody
    public AssetErrorMessage handleMultipartException() {
        return AssetErrorMessage.builder()
                .message("Total file size should not exceed " + maxFileSize)
                .build();
    }

    @ExceptionHandler(AssetUnauthorizedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public AssetErrorMessage handleUnauthorizedException(AssetUnauthorizedException e) {
        return AssetErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }


    @ExceptionHandler(AssetNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public AssetErrorMessage handleAssetNotFoundException(AssetNotFoundException e) {
        return AssetErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(AssetException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public AssetErrorMessage handleAssetException(AssetException e) {
        return AssetErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(VideoUploadException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public AssetErrorMessage handleAssetException(VideoUploadException e) {
        return AssetErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }
}
