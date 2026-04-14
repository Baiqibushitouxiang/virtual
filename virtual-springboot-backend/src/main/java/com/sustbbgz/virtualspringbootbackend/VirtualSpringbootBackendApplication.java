package com.sustbbgz.virtualspringbootbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VirtualSpringbootBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualSpringbootBackendApplication.class, args);
    }

}
