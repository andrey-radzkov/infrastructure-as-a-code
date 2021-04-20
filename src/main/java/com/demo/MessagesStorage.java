package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.springframework.web.context.WebApplicationContext.SCOPE_APPLICATION;

@Component
@Scope(SCOPE_APPLICATION)
public class MessagesStorage {
    private static final Logger log = LoggerFactory.getLogger(MessagesStorage.class);

    private final Map<String, Order> messages = new ConcurrentHashMap<>();
    private final Map<String, Order> processedMessages = new ConcurrentHashMap<>();
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;

    public Map<String, Order> getMessages() {
        return messages;
    }

    public Map<String, Order> getProcessedMessages() {
        return processedMessages;
    }

    @Scheduled(fixedDelay = 2197L)
    public void processMessages() throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                List<String> toDelete = new ArrayList<>();
                getMessages().forEach((key, value) -> {
                    value.setShipNumber(Integer.toString(new Random().nextInt(5)));
                    value.setContainerNumber(new Random().nextInt(20) + "-" + new Random().nextInt(20) + "-" + new Random().nextInt(10));
                    value.setStartDate(new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toString());
                    value.setEndDate(new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(7).toString());
                    getProcessedMessages().put(key, value);
                    toDelete.add(key);
                    log.info(key);
                });
                toDelete.forEach(getMessages()::remove);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }, demoExecutor).get();
    }
}
