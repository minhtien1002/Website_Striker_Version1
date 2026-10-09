package com.example.sd_24_striker_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.sd_24_striker_be")
public class
Sd24StrikerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sd24StrikerBeApplication.class, args);
    }

}
