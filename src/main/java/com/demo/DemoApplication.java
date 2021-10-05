package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
@RestController
public class DemoApplication {
    private List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
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
