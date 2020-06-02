package com.demo;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.util.EC2MetadataUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        AmazonElasticLoadBalancing loadBalancing = AmazonElasticLoadBalancingClientBuilder.defaultClient();
        RegisterInstancesWithLoadBalancerRequest instanceRequest = new RegisterInstancesWithLoadBalancerRequest();
        instanceRequest.setLoadBalancerName("tf-example-lb-ec2");
        instanceRequest.setInstances(Collections.singletonList(new Instance(EC2MetadataUtils.getInstanceId())));
        loadBalancing.registerInstancesWithLoadBalancer(instanceRequest);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

}
