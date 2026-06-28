package com.smartdealhub.smartdealhub;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
        @ComponentScan(basePackages  = "com.smartdealhub.smartdealhub")
public class SmartdealhubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartdealhubApplication.class, args);
        System.out.println("SmartDeal Hub backend started successfully!");
    }
}