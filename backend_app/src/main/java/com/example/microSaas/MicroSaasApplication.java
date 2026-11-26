package com.example.microSaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MicroSaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroSaasApplication.class, args);
    }
}