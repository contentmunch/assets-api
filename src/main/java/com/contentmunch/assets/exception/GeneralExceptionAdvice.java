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
public class GeneralExceptionAdvice {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;


    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    @ResponseBody
    public ErrorMessage handleMultipartException() {
        return ErrorMessage.builder()
                .message("Total file size should not exceed " + maxFileSize)
                .build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorMessage handleUnauthorizedException(UnauthorizedException e) {
        return ErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }


    @ExceptionHandler(AssetNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorMessage handleAssetNotFoundException(AssetNotFoundException e) {
        return ErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(AssetException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorMessage handleAssetException(AssetException e) {
        return ErrorMessage.builder()
                .message(e.getMessage())
                .build();
    }
}
