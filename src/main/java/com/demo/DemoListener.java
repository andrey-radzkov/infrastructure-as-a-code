package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class DemoListener {
    private static final Logger log = LoggerFactory.getLogger(DemoListener.class);
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;

    @SqsListener(value = "demo-queue.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String message) throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
                log.info(message);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }, demoExecutor).get();
    }
}
