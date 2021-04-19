package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@RestController
@Scope(SCOPE_SESSION)
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);
    @Qualifier("demoExecutor")
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;

    private final Map<String, Order> messages = new ConcurrentHashMap<>();
    private final Map<String, Order> processedMessages = new ConcurrentHashMap<>();

    @GetMapping("/order")
    public String syncOrder() throws IOException {
        return getPage("order.html").replace("#METHOD", "/order");
    }

    @GetMapping("/order-async")
    public String asyncOrder() throws IOException {
        return getPage("order.html").replace("#METHOD", "/order-async");
    }

    @GetMapping("/check")
    public String check(@CookieValue(value = "orderID", defaultValue = "", required = false) String orderID) throws IOException {
        Order order = processedMessages.get(orderID);
        if (order != null) {
            return getPage("ordered.html")
                    .replace("#MESSAGE", "Комплектация завершена успешно")
                    .replace("#ORDER_NUMBER", order.getNumber())
                    .replace("#SHIP_NUMBER", order.getShipNumber())
                    .replace("#CONTAINER_NUMBER", order.getContainerNumber())
                    .replace("#START_DATE", order.getStartDate())
                    .replace("#END_DATE", order.getEndDate())
                    .replace("#EMAIL", order.getEmail())
                    .replace("#ORDER_LINK", "/order-async");
        } else {
            return getPage("ordered.html")
                    .replace("<div class=\"mark\"></div>", "")
                    .replace("#MESSAGE", "Комплектация не завершена. Проверьте, пожалуйста, позже")
                    .replace("#ORDER_NUMBER", "")
                    .replace("#SHIP_NUMBER", "")
                    .replace("#CONTAINER_NUMBER", "")
                    .replace("#START_DATE", "")
                    .replace("#END_DATE", "")
                    .replace("#EMAIL", "")
                    .replace("#ORDER_LINK", "/order-async");
        }
    }

    @PostMapping(value = "/order-async", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String sendOrder(HttpServletResponse response, Order order) throws IOException {
        final String uiud = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("orderID", uiud);
        response.addCookie(cookie);
        order.setNumber(uiud);
        messages.put(uiud, order);
        return getPage("async.html")
                .replace("#ORDER_NUMBER", uiud);
    }

    @PostMapping(value = "/order", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String addOrder(Order order) throws InterruptedException, ExecutionException, IOException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, demoExecutor).get();
        return getPage("ordered.html")
                .replace("#MESSAGE", "Комплектация завершена успешно")
                .replace("#ORDER_NUMBER", UUID.randomUUID().toString())
                .replace("#SHIP_NUMBER", Integer.toString(new Random().nextInt(5)))
                .replace("#CONTAINER_NUMBER", new Random().nextInt(20) + "-" + new Random().nextInt(20) + "-" + new Random().nextInt(10))
                .replace("#START_DATE", new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toString())
                .replace("#END_DATE", new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(7).toString())
                .replace("#EMAIL", order.getEmail())
                .replace("#ORDER_LINK", "/order");
    }

    @Scheduled(fixedDelay = 4997L)
    public void processMessages() throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                List<String> toDelete = new ArrayList<>();
                messages.forEach((key, value) -> {
                    value.setShipNumber(Integer.toString(new Random().nextInt(5)));
                    value.setContainerNumber(new Random().nextInt(20) + "-" + new Random().nextInt(20) + "-" + new Random().nextInt(10));
                    value.setStartDate(new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toString());
                    value.setEndDate(new Date().toInstant().atZone(ZoneId.systemDefault()).plusDays(7).toString());
                    processedMessages.put(key, value);
                    toDelete.add(key);
                    log.info(key);
                });
                toDelete.forEach(messages::remove);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }, demoExecutor).get();
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

    private String getPage(String page) throws IOException {
        String data;
        ClassPathResource cpr = new ClassPathResource(page);
        try (InputStream inputStream = cpr.getInputStream()) {
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            data = new String(bdata, StandardCharsets.UTF_8);
        }
        return data;
    }
}
