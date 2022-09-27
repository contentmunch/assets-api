package com.contentmunch.assets.configuration;

import com.contentmunch.assets.controller.AssetController;
import com.contentmunch.assets.controller.AssetsController;
import com.contentmunch.assets.data.assembler.AssetAssembler;
import com.contentmunch.assets.data.assembler.AssetFolderAssembler;
import com.contentmunch.assets.exception.AssetsApiExceptionAdvice;
import com.contentmunch.assets.service.AssetService;
import com.contentmunch.assets.service.GoogleDriveService;
import com.contentmunch.assets.service.PropertyService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(basePackageClasses = {AssetController.class, AssetsController.class,
        AssetsApiExceptionAdvice.class, AssetFolderAssembler.class, AssetAssembler.class, PropertyService.class,
        AssetService.class, GoogleDriveService.class, AssetDriveConfig.class, AssetCachingConfiguration.class})
@Configuration
@PropertySource(value = "classpath:assets-api-application.yml", factory = YamlPropertySourceFactory.class)
public class AssetsApiConfiguration {
}
