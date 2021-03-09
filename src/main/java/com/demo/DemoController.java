package com.demo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@RestController
@Scope(SCOPE_SESSION)
public class DemoController {
    private String name;
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private AmazonSQSAsync amazonSqs;

    @PostConstruct
    public void init() {
        this.queueMessagingTemplate = new QueueMessagingTemplate(amazonSqs);
    }

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
    public String sendName(@RequestParam(value = "name") String name) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("message-group-id", "1");
            queueMessagingTemplate.convertAndSend("demo-queue.fifo", name, headers);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "will process soon";
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }
}
