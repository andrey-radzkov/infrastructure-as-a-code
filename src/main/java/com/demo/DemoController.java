package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Random;
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

    @GetMapping("/order")
    public String syncOrder() throws IOException {
        return getPage("sync.html");
    }

    private String getPage(String page) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + page);
        return new String(Files.readAllBytes(file.toPath()));
    }

    @PostMapping(value = "/order", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String addOrder(Order order) throws InterruptedException, ExecutionException, IOException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, demoExecutor).get();
        return getPage("sync-ordered.html")
                .replace("#ORDER_NUMBER", UUID.randomUUID().toString())
                .replace("#SHIP_NUMBER", Integer.toString(new Random().nextInt(5)))
                .replace("#CONTAINER_NUMBER", new Random().nextInt(20) + "-" + new Random().nextInt(20) + "-" + new Random().nextInt(20))
                .replace("#START_DATE", new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toString())
                .replace("#END_DATE", new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(7).toString())
                .replace("#EMAIL", order.getEmail());
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
