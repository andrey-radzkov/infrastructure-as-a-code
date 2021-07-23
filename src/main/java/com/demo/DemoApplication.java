package com.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@EnableKafka
@SpringBootApplication
@RestController
public class DemoApplication {
    private List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        kafkaTemplate.send("test2", "hello");
//        kafkaTemplate.metrics();
        return String.format("Hello %s!", name);
    }

    @KafkaListener(topics = "test2", groupId = "groupId")
    public void listen(String in) {
        System.out.println(in);
    }

    @PostMapping("/event")
    public void receiveEvent(@RequestBody Map<String, Object> event) {
        events.add(event);
    }

    @GetMapping("/event")
    public List<Map<String, Object>> getEvent() {
        return events;
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

}
