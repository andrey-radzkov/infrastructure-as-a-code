package com.demo;

import io.confluent.ksql.api.client.*;
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
import java.util.concurrent.ExecutionException;

@EnableKafka
@SpringBootApplication
@RestController
public class DemoApplication {
    private List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    public static String KSQLDB_SERVER_HOST = "localhost";
    public static int KSQLDB_SERVER_HOST_PORT = 8088;
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        kafkaTemplate.send("test2", "{\"name\": \"" + name + "\",\"other-field\": \"other-value\"}");
//        kafkaTemplate.metrics();
        return String.format("Hello %s!", name);
    }

    @GetMapping("/ksql")
    public String ksql(@RequestParam(value = "query") String query) throws ExecutionException, InterruptedException {
        return execute(query);
    }

    @PostMapping("/ksql")
    public String ksqlPost(@RequestBody Map<String, String> query) throws ExecutionException, InterruptedException {
        return execute(query.get("query"));
    }

    private String execute(String query) throws InterruptedException, ExecutionException {
        ClientOptions options = ClientOptions.create()
                .setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT);
        Client client = Client.create(options);

        // Send requests with the client by following the other examples
        StreamedQueryResult queryResult = client.streamQuery(query).get();

// Wait for query result
        final Row poll = queryResult.poll();
        // Terminate any open connections nd close the client
        client.close();
//        kafkaTemplate.metrics();
        return String.format("Hello %s!", poll.getInteger("C_N"));
    }

    @KafkaListener(topics = "test2", groupId = "groupId")
    public void listen(String in) {
        System.out.println("From kafka: " + in);
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
