package com.demo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
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
        queueMessagingTemplate.send("demo-queue.fifo", MessageBuilder.withPayload(name).build());
        return "will process soon";
    }

    @SqsListener(value = "demo-queue.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String message) throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
                System.out.println(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, demoExecutor).get();
        //TODO: think about behavior
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }
}
