package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@RestController
@Scope(SCOPE_SESSION)
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private String name;
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;

    private final Map<String, QueueMessage> messages = new ConcurrentHashMap<>();
    private final Map<String, QueueMessage> processedMessages = new ConcurrentHashMap<>();


    @GetMapping("/get-name")
    public String getName() throws InterruptedException, ExecutionException {

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, demoExecutor).get();
        return "hello " + this.name;
    }

    @GetMapping("/get-name-async")
    public String getNameAsync(@CookieValue(value = "JSESSIONID", defaultValue = "", required = false) String sessionId) {

        QueueMessage queueMessage = processedMessages.get(sessionId);
        return queueMessage != null ? "hello " + queueMessage.getMessage() : "your message was not processed";
    }

    @GetMapping("/set-name")
    public String setName(@RequestParam(value = "name") String name) throws InterruptedException, ExecutionException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, demoExecutor).get();
        this.name = name;
        return name;
    }

    @GetMapping("/send-name")
    public String sendName(@RequestParam(value = "name") String name, @CookieValue(value = "JSESSIONID", defaultValue = "", required = false) String sessionId) {
        try {
            QueueMessage queueMessage = new QueueMessage(name, sessionId != null ? sessionId : UUID.randomUUID().toString());
            messages.put(sessionId, queueMessage);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "will process soon";
    }

    @Scheduled(fixedDelay = 1000L)
    public void processMessages() throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
                messages.forEach((key, value) -> {
                    processedMessages.put(key, value);
                    messages.remove(key);
                    log.info(key);
                });
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }, demoExecutor).get();
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }
}
