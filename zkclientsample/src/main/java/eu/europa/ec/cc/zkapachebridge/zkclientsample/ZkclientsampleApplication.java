package eu.europa.ec.cc.zkapachebridge.zkclientsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ZkclientsampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkclientsampleApplication.class, args);
    }
}
