package com.notification.microservice.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class NotificationService
{

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @SqsListener(value = "${sqs.queue.url}")
    public void receiveMessage(String message) {
        logger.info("Received message from SQS" + message);
        System.out.println("Received message from SQS: " + message);
    }

}
