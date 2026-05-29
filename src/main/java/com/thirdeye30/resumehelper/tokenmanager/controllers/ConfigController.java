package com.thirdeye30.resumehelper.tokenmanager.controllers;

import com.thirdeye30.resumehelper.tokenmanager.dtos.ConfigDto;
import com.thirdeye30.resumehelper.tokenmanager.services.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tokenmanager/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @PutMapping
    public ResponseEntity<ConfigDto> updateConfig(@RequestBody ConfigDto configDto) {
        return ResponseEntity.ok(configService.updateConfig(configDto));
    }

    @GetMapping
    public ResponseEntity<ConfigDto> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }
}
