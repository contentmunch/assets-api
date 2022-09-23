package com.contentmunch.assets.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class AssetCachingConfiguration {
    public static final String ASSETS_CACHE = "assets";

    @Bean
    public Caffeine caffeineConfig() {
        var caffeine = Caffeine.newBuilder().initialCapacity(100);
        caffeine.maximumSize(500);
        return caffeine;
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {

        var manager = new CaffeineCacheManager(ASSETS_CACHE);
        manager.setCaffeine(caffeine);
        return manager;
    }
}
