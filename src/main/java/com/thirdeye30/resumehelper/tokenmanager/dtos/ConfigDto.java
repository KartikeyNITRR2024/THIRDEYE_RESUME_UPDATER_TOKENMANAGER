package com.thirdeye30.resumehelper.tokenmanager.dtos;

import lombok.Data;

@Data
public class ConfigDto {
    private Long maximumTokenAllocated;
    private Integer maximumTimeForUserInDays;
}
