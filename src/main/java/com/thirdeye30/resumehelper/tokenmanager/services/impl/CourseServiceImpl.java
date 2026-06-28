package com.thirdeye30.resumehelper.tokenmanager.services.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thirdeye30.resumehelper.tokenmanager.services.MessageBrokerService;

import jakarta.persistence.EntityNotFoundException;

import com.thirdeye30.resumehelper.tokenmanager.dtos.Message;
import com.thirdeye30.resumehelper.tokenmanager.dtos.PriorityDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thirdeye30.resumehelper.tokenmanager.dtos.ConfigDto;
import com.thirdeye30.resumehelper.tokenmanager.dtos.CourseDto;
import com.thirdeye30.resumehelper.tokenmanager.dtos.CoursePayload;
import com.thirdeye30.resumehelper.tokenmanager.entities.Course;
import com.thirdeye30.resumehelper.tokenmanager.enums.CourseStatus;
import com.thirdeye30.resumehelper.tokenmanager.enums.MailType;
import com.thirdeye30.resumehelper.tokenmanager.enums.Status;
import com.thirdeye30.resumehelper.tokenmanager.repos.CourseRepository;
import com.thirdeye30.resumehelper.tokenmanager.services.ConfigService;
import com.thirdeye30.resumehelper.tokenmanager.services.CourseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
	
	private final ConfigService configService;
	private final CourseRepository courseRepository;
	private final MessageBrokerService messageBrokerService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    
    @Value("${thirdeye.redis.course-prefix}")
    private String redisCoursePrefix;
	
	public long getTodaysCount() {
		LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
	    List<CourseStatus> targetStatuses = List.of(CourseStatus.PROCESSING, CourseStatus.COURSE_CREATION_COMPLETED, CourseStatus.EXTRACT_COMPLETED);
	    return courseRepository.countByCreateTimeAfterAndStatusIn(startOfToday, targetStatuses);
    }
	
	public long getTodaysCountForUser(String email) {
	    LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
	    List<CourseStatus> targetStatuses = List.of(CourseStatus.PROCESSING, CourseStatus.COURSE_CREATION_COMPLETED, CourseStatus.EXTRACT_COMPLETED);
	    return courseRepository.countByEmailAndDateAndStatusIn(email, startOfToday, targetStatuses);
	}
	
	@Override
	@Transactional
	public CourseDto createCourse(CourseDto courseDto) {
	    log.info("Attempting to create a new course request for email: {}", courseDto.getEmail());
	    
	    ConfigDto config = configService.getConfig();
	    
	    long currentGlobalCount = getTodaysCount();
	    if (currentGlobalCount >= config.getMaximumCourseCreatedPerDay()) {
	        log.warn("Global daily limit reached: {}/{}", currentGlobalCount, config.getMaximumCourseCreatedPerDay());
	        throw new RuntimeException("Maximum courses created for today");
	    }
	    
	    long currentUserCount = getTodaysCountForUser(courseDto.getEmail());
	    if (currentUserCount >= config.getMaximumNumberofCourseCreatedPerDayPerUser()) {
	        log.warn("User daily limit reached for {}: {}/{}", 
	            courseDto.getEmail(), currentUserCount, config.getMaximumNumberofCourseCreatedPerDayPerUser());
	        throw new RuntimeException("User daily limit exhausted. Try with another email.");
	    }

	    Course course = Course.builder()
	            .email(courseDto.getEmail())
	            .yoe(courseDto.getYoe())
	            .company(courseDto.getCompany())
	            .jobDescription(courseDto.getJobDescription())
	            .jobUrl(courseDto.getJobUrl())
	            .courseStatus(CourseStatus.PROCESSING)
	            .build();
	    
	    Course savedCourse = courseRepository.save(course);
	    log.info("Successfully created course with ID: {} for user: {}", savedCourse.getId(), savedCourse.getEmail());
	    messageBrokerService.sendMessages("aiprocesser2", savedCourse);
	    return convertToDto(savedCourse);
	}
	
	@Override
	public CourseDto getCourseById(UUID id) {
	    return courseRepository.findById(id)
	            .map(this::convertToDto)
	            .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
	}

	@Override
	public List<CourseDto> getCoursesByEmail(String email) {
	    return courseRepository.findByEmail(email).stream()
	            .map(this::convertToDto)
	            .toList();
	}
	
	@Override
	public void updateStatusInBatch() {
	    while (true) {
	        try {
	            List<Message<PriorityDto>> messages = messageBrokerService.getMessage("priorityskills");
	            if (messages == null || messages.isEmpty()) {
	                break;
	            }

	    	    List<CoursePayload> coursePayloadList1 = new ArrayList<>();
	    	    List<CoursePayload> coursePayloadList2 = new ArrayList<>();
	            for (Message<PriorityDto> message : messages) {
	                try {
	                	CoursePayload coursePayload = processSingleMessage(message);
	                	if(coursePayload!=null &&  coursePayload.getStatus().equals(Status.EXTRACTION_COMPLETED))
	                	{
	                		coursePayloadList1.add(coursePayload);
	                	}
	                	else if(coursePayload!=null &&  coursePayload.getStatus().equals(Status.COURSE_CREATION_COMPLETED))
	                	{
	                		coursePayloadList2.add(coursePayload);
	                	}
	                } catch (Exception e) {
	                    log.error("Failed to process message with ID: {}. Error: {}", 
	                              message.getMessage().getId(), e.getMessage());
	                }
	            }
	            if(!coursePayloadList1.isEmpty())
		        {
		        	messageBrokerService.sendMultipleMessages("courseprocesser", coursePayloadList1);
		        	log.info("Sending {} mails to course processer", coursePayloadList1.size());
		        }
		        if(!coursePayloadList2.isEmpty())
		        {
		        	messageBrokerService.sendMultipleMessages("mailprocesser", coursePayloadList2);
		        	log.info("Sending {} mails to mail processer", coursePayloadList2.size());
		        }
	        } catch (Exception ex) {
	            log.error("Critical error in message broker retrieval loop", ex);
	            break;
	        }
	    }
	}
	
	public Map<String, List<String>> getResultAsMap(String jsonResult) {
	    try {
	        return objectMapper.readValue(jsonResult, new TypeReference<Map<String, List<String>>>(){});
	    } catch (JsonProcessingException e) {
	        log.error("Failed to parse result JSON", e);
	        return Collections.emptyMap();
	    }
	}
	
	public Map<String, List<List<String>>> getResultAsMap1(String jsonResult) {
	    try {
	        return objectMapper.readValue(jsonResult, new TypeReference<Map<String, List<List<String>>>>(){});
	    } catch (JsonProcessingException e) {
	        log.error("Failed to parse result JSON", e);
	        return Collections.emptyMap();
	    }
	}

	private CoursePayload processSingleMessage(Message<PriorityDto> message) {
	    PriorityDto priority = message.getMessage();
	    log.info("priorityDto is {}", priority.toString());
	    CoursePayload coursePayload = null;
	    Course course = courseRepository.findById(priority.getId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + priority.getId()));
	    if (priority.getStatus().equals(Status.EXTRACTION_COMPLETED)) {
	        course.setCourseStatus(CourseStatus.EXTRACT_COMPLETED);
		    Map<String, List<String>> resultData = new HashMap<>();
	        resultData.put("highPriority", priority.getHighPriority());
	        resultData.put("mediumPriority", priority.getMediumPriority());
	        resultData.put("lowPriority", priority.getLowPriority());
	        String jsonResult = "";
			try {
				jsonResult = objectMapper.writeValueAsString(resultData);
				course.setCourseStatus(CourseStatus.EXTRACT_COMPLETED);
			} catch (JsonProcessingException e) {
				course.setCourseStatus(CourseStatus.FAILED);
				log.error("Error in saving data");
			}
	        course.setResult(jsonResult);
	        coursePayload = new CoursePayload(course.getId() ,null, null, null, priority.getHighPriority(),
					priority.getMediumPriority(), priority.getLowPriority(), course.getCompany(), priority.getStatus());
	    }
	    else if (priority.getStatus().equals(Status.COURSE_CREATION_COMPLETED))
	    {
	    	Map<String, List<String>> priorityMap = getResultAsMap(course.getResult());
	    	course.setCourseStatus(CourseStatus.COURSE_CREATION_COMPLETED);
	    	String jsonResult = course.getResult();
			try {
				jsonResult = objectMapper.writeValueAsString(priority.getCoursePath());
				course.setCourseStatus(CourseStatus.EXTRACT_COMPLETED);
			} catch (JsonProcessingException e) {
				course.setCourseStatus(CourseStatus.FAILED);
				log.error("Error in saving data");
			}
	    	course.setCourseResult(jsonResult);
	    	coursePayload = new CoursePayload(course.getId() ,MailType.COURSE, course.getEmail(), "", priorityMap.get("highPriority"),
	    			priorityMap.get("mediumPriority"), priorityMap.get("lowPriority"), course.getCompany(), priority.getStatus());
	    } 
	    else if (priority.getStatus().equals(Status.MAILED_COMPLETED))
	    {
	    	course.setCourseStatus(CourseStatus.MAILED_COMPLETED);
	    	try {
	            CourseDto courseDto = convertToDto(course);
	            String cacheKey = redisCoursePrefix + "content:" + course.getId().toString();
	            String jsonToCache = objectMapper.writeValueAsString(courseDto);
	            redisTemplate.opsForValue().set(cacheKey, jsonToCache, Duration.ofMinutes(30));
	        } catch (JsonProcessingException e) {
	            log.error("Failed to serialize and cache course {} in Redis", course.getId(), e);
	        }
	    } 
	    else if (List.of(Status.COURSE_CREATION_FAILED, Status.MAILED_FAILED, Status.EXTRACTION_FAILED).contains(priority.getStatus()))
	    {
	        course.setCourseStatus(CourseStatus.FAILED);
	    }
        courseRepository.save(course);
        return coursePayload;
	}
	

	private CourseDto convertToDto(Course course) {
	    CourseDto dto = new CourseDto();
	    dto.setId(course.getId());
	    dto.setEmail(course.getEmail());
	    dto.setCompany(course.getCompany());
	    dto.setJobUrl(course.getJobUrl());
	    dto.setJobDescription(course.getJobDescription());
	    dto.setCourseStatus(course.getCourseStatus());
	    dto.setCreateTime(course.getCreateTime());
	    dto.setResult((course.getResult()!=null && course.getResult().length()>0)?getResultAsMap(course.getResult()):null);
	    dto.setCourseResult((course.getCourseResult()!=null && course.getCourseResult().length()>0)?getResultAsMap1(course.getCourseResult()):null);
	    return dto;
	}
	
	

}
