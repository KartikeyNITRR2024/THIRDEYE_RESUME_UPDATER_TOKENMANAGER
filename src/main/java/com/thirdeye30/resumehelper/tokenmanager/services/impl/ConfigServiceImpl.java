package com.thirdeye30.resumehelper.tokenmanager.services.impl;

import org.springframework.beans.factory.annotation.Value; // Import this
import org.springframework.stereotype.Service;
import com.thirdeye30.resumehelper.tokenmanager.dtos.ConfigDto;
import com.thirdeye30.resumehelper.tokenmanager.entities.Config;
import com.thirdeye30.resumehelper.tokenmanager.repos.ConfigRepository;
import com.thirdeye30.resumehelper.tokenmanager.services.ConfigService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private static final String CONFIG_ID = "GLOBAL_CONFIG";

    @Value("${config.default.max-tokens:10}") 
    private Long defaultMaxTokens;

    @Value("${config.default.max-days:1}")
    private Integer defaultMaxDays;

    private volatile ConfigDto cachedConfig;

    @PostConstruct
    public void init() {
        log.info("Initializing configuration cache...");
        refreshCache();
    }

    @Override
    public ConfigDto updateConfig(ConfigDto configDto) {
        log.info("Updating DB and Cache with: {}", configDto);
        Config config = Config.builder()
                .id(CONFIG_ID)
                .maximumTokenAllocated(configDto.getMaximumTokenAllocated())
                .maximumTimeForUserInDays(configDto.getMaximumTimeForUserInDays())
                .build();
        
        configRepository.save(config);
        this.cachedConfig = configDto;
        return this.cachedConfig;
    }

    @Override
    public ConfigDto getConfig() {
        return cachedConfig;
    }

    private void refreshCache() {
        Config config = configRepository.findById(CONFIG_ID)
                .orElseGet(() -> {
                    log.warn("No DB config found. Using properties: Tokens={}, Days={}", 
                              defaultMaxTokens, defaultMaxDays);
                    return new Config(CONFIG_ID, defaultMaxTokens, defaultMaxDays);
                });
        this.cachedConfig = mapToDto(config);
    }

    private ConfigDto mapToDto(Config config) {
        ConfigDto dto = new ConfigDto();
        dto.setMaximumTokenAllocated(config.getMaximumTokenAllocated());
        dto.setMaximumTimeForUserInDays(config.getMaximumTimeForUserInDays());
        return dto;
    }
}