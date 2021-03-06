package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@SpringBootApplication
@RestController
@Scope(SCOPE_SESSION)
public class DemoApplication {
    private String name;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/get-name")
    public String getName() throws InterruptedException {
        Thread.sleep(1500);
        return "hello " + this.name;
    }

    @GetMapping("/set-name")
    public String setName(@RequestParam(value = "name") String name) throws InterruptedException {
        Thread.sleep(1500);
        this.name = name;
        return name;
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

}
