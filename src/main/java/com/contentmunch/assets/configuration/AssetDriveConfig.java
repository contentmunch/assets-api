package com.contentmunch.assets.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.drive")
public class AssetDriveConfig {
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String tokenServer;
    private String applicationName;
    private String defaultFolder;
}
