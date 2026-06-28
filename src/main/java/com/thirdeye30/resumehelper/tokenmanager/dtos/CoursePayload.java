package com.thirdeye30.resumehelper.tokenmanager.dtos;

import java.util.List;
import java.util.UUID;

import com.thirdeye30.resumehelper.tokenmanager.enums.MailType;
import com.thirdeye30.resumehelper.tokenmanager.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoursePayload {
	private UUID id;
	private MailType mailType;
	private String email;
	private String courseUrl;
	private List<String> highPriority;
	private List<String> mediumPriority;
	private List<String> lowPriority;
	private String company;
	private Status status;
}
