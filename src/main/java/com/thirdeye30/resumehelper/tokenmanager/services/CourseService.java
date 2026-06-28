package com.thirdeye30.resumehelper.tokenmanager.services;

import java.util.List;
import java.util.UUID;

import com.thirdeye30.resumehelper.tokenmanager.dtos.CourseDto;

public interface CourseService {

	CourseDto createCourse(CourseDto courseDto);

	CourseDto getCourseById(UUID id);

	List<CourseDto> getCoursesByEmail(String email);

	void updateStatusInBatch();

}
