package com.thirdeye30.resumehelper.tokenmanager.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirdeye30.resumehelper.tokenmanager.dtos.CourseDto;
import com.thirdeye30.resumehelper.tokenmanager.services.CourseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tokenmanager/v1/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;
	
	@PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
		CourseDto createdCourse = courseService.createCourse(courseDto);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
}
