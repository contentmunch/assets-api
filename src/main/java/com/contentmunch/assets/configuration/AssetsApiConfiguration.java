package com.contentmunch.assets.configuration;

import com.contentmunch.assets.data.AssetAssembler;
import com.contentmunch.assets.data.AssetFolderAssembler;
import com.contentmunch.assets.exception.AssetsApiExceptionAdvice;
import com.contentmunch.assets.service.AssetService;
import com.contentmunch.assets.service.GoogleDriveService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(basePackageClasses = {AssetsApiExceptionAdvice.class, AssetFolderAssembler.class,
        AssetAssembler.class, AssetService.class, GoogleDriveService.class, AssetDriveConfig.class})
@Configuration
@PropertySource(value = "classpath:assets-api-application.yml", factory = YamlPropertySourceFactory.class)
public class AssetsApiConfiguration {
}
