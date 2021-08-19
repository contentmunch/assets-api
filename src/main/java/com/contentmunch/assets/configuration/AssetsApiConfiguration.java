package com.contentmunch.assets.configuration;

import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.exception.AssetsApiExceptionAdvice;
import com.contentmunch.assets.service.AssetsService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(basePackageClasses = {AssetsController.class, AssetsApiExceptionAdvice.class, AssetsService.class, AssetDriveConfig.class})
@Configuration
@PropertySource(value = "classpath:assets-api-application.yml", factory = YamlPropertySourceFactory.class)
public class AssetsApiConfiguration {
}
