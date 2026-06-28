package com.thirdeye30.resumehelper.tokenmanager.dtos;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.thirdeye30.resumehelper.tokenmanager.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class PriorityDto {
	private UUID id;
	private Status status;
	private List<String> highPriority;
	private List<String> mediumPriority;
	private List<String> lowPriority;
	private Map<String, List<List<String>>> coursePath;
}
