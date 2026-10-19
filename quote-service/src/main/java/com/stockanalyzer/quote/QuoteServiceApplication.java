package com.stockanalyzer.quote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class QuoteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteServiceApplication.class, args);
    }
}
