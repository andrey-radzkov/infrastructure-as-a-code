package com.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyUtil {
    /**
     * Limit is 500 calls per day per api key
     * new keys: https://www.alphavantage.co/support/#api-key
     */
    public static final int DAILY_LIMIT = 500; //TODO: return to 500, store usage for repeatable run
    public static final int MINUTE_LIMIT = 5;
    public static List<String> keys;

    static {
        try {
            keys = Files.readAllLines(Paths.get("D:/projects/infrastructure-as-a-code/src/main/resources/keys.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static final Map<String, Integer> KEY_USAGE_COUNT = keys.stream().collect(Collectors.toMap(value -> value, value -> 0));

    public static String getApiKey() {
        try {
            Thread.sleep(75000 / MINUTE_LIMIT / KEY_USAGE_COUNT.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Map.Entry<String, Integer> currentEntry = KEY_USAGE_COUNT.entrySet().stream().min(Map.Entry.comparingByValue()).get();
        if (currentEntry.getValue() == DAILY_LIMIT) {
            System.exit(0);
        }
        KEY_USAGE_COUNT.put(currentEntry.getKey(), currentEntry.getValue() + 1);
        return currentEntry.getKey();
    }
}
