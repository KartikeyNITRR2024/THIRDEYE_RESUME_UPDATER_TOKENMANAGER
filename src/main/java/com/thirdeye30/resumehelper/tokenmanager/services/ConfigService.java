package com.thirdeye30.resumehelper.tokenmanager.services;

import com.thirdeye30.resumehelper.tokenmanager.dtos.ConfigDto;

public interface ConfigService {
    ConfigDto updateConfig(ConfigDto configDto);
    ConfigDto getConfig();
}