package com.thirdeye30.resumehelper.tokenmanager.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.thirdeye30.resumehelper.tokenmanager.enums.CourseStatus;

import lombok.Data;

@Data
public class CourseDto {
	private UUID id;
    private String email;
    private String yoe;
    private String company;
    private String jobDescription;
    private String jobUrl;
    private CourseStatus courseStatus;
    private Map<String, List<String>> result;
    private Map<String, List<List<String>>> courseResult;
    private LocalDateTime createTime;
}
