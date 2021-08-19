package com.contentmunch.assets.annotation;

import com.contentmunch.assets.configuration.AssetsApiConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AssetsApiConfiguration.class)
public @interface EnableAssetsApi {
}
