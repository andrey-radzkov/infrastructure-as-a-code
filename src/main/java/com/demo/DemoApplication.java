package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class DemoApplication {
    public static final String STREAM_NAME = "test";
    private List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
    private KinesisAsyncClient kinesisClient;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostConstruct
    public void init() {
        Region region = Region.US_EAST_1;
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(100)
                .maxPendingConnectionAcquires(10_000)
                .build();
        kinesisClient = KinesisAsyncClient.builder().httpClient(httpClient)
                .region(region)
                .build();
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @PostMapping("/event")
    public void receiveEvent(@RequestBody Map<String, Object> event) {
        events.add(event);
    }

    @PutMapping("/event")
    public void sendEvent() {

        String message = "{\"message\": \"message with date\", \"messageDate\": \"" + new Date().toString() + "\"}";
        PutRecordRequest request = PutRecordRequest.builder()
                .partitionKey("1") // We use the ticker symbol as the partition key, explained in the Supplemental Information section below.
                .streamName(STREAM_NAME)
                .data(SdkBytes.fromByteArray(message.getBytes()))
                .build();
        kinesisClient.putRecord(request);
        System.out.println("Sending message: " + message);
    }

    @GetMapping("/event")
    public List<String> getEvent() throws ExecutionException, InterruptedException {
//    public List<Map<String, Object>> getEvent() throws ExecutionException, InterruptedException {
        String shardIterator;
        String lastShardId = null;

        // Retrieve the shards from a data stream
        DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
                .streamName(STREAM_NAME)
                .build();
        List<Shard> shards = new ArrayList<>();

        DescribeStreamResponse streamRes;
        do {
            streamRes = kinesisClient.describeStream(describeStreamRequest).get();
            shards.addAll(streamRes.streamDescription().shards());

            if (shards.size() > 0) {
                lastShardId = shards.get(shards.size() - 1).shardId();
            }
        } while (streamRes.streamDescription().hasMoreShards());

        GetShardIteratorRequest itReq = GetShardIteratorRequest.builder()
                .streamName(STREAM_NAME)
                .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                .shardId(shards.get(0).shardId())
                .build();

        GetShardIteratorResponse shardIteratorResult = kinesisClient.getShardIterator(itReq).get();
        shardIterator = shardIteratorResult.shardIterator();


        // Create a GetRecordsRequest with the existing shardIterator,
        // and set maximum records to return to 1000
        GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                .shardIterator(shardIterator)
                .limit(100)
                .build();

        GetRecordsResponse result = kinesisClient.getRecords(recordsRequest).get();

        // Put result into a record list, result might be empty
        List<Record> records = result.records();

        return records.stream()
                .map(record -> new String(record.data().asByteArray()))
                .collect(Collectors.toList());
//        return events;
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

}
