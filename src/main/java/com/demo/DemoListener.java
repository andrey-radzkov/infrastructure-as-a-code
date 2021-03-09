package com.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class DemoListener {
    private static final Logger log = LoggerFactory.getLogger(DemoListener.class);
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;
    private final Map<String, SqsMessage> messages = new HashMap<>();

    @SqsListener(value = "demo-queue.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String message) throws ExecutionException, InterruptedException {
        //TODO: list of messages
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
                SqsMessage sqsMessage = new ObjectMapper().readValue(message, SqsMessage.class);
                messages.put(sqsMessage.getSessionId(), sqsMessage);
                log.info(message);
            } catch (InterruptedException | JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }, demoExecutor).get();
    }

    public Map<String, SqsMessage> getMessages() {
        return messages;
    }
}
