package com.thirdeye30.resumehelper.tokenmanager.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class UserDto {
	private UUID id;
    private String name;
    private String email;
    private Long token;
    private LocalDateTime createTime;
}
