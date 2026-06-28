package com.thirdeye30.resumehelper.tokenmanager.services;

import java.util.List;

import com.thirdeye30.resumehelper.tokenmanager.dtos.Message;
import com.thirdeye30.resumehelper.tokenmanager.dtos.PriorityDto;

public interface MessageBrokerService {

	void sendMessages(String topicName, Object messagess);

	List<Message<PriorityDto>> getMessage(String topicName);

    void sendMultipleMessages(String topicName, Object messages);

}

