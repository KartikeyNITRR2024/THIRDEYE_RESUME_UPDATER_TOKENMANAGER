package com.thirdeye30.resumehelper.tokenmanager.utils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.thirdeye30.resumehelper.tokenmanager.services.CourseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {
    private volatile boolean isWorking = false;
    private final CourseService courseService;

    @Scheduled(fixedRateString = "${thirdeye.message.broker.read.rate}")
    public void readMessagesFromMessageBroker() {
        synchronized (this) {
            if (isWorking) {
                return; 
            }
            isWorking = true;
        }
        try {
        	courseService.updateStatusInBatch();
            log.info("Starting to read message from broker...");
        } catch (Exception e) {
            log.error("Error occurred while reading messages: ", e);
        } finally {
            isWorking = false;
        }
    }
    
//    @Scheduled(fixedRate = 300000)
//    public void uploadStaleDocument() {
//    	log.info("Uploading stale resumes...");
//    	resumeService.processStaleUpdates();
//    }
}