package com.bom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.bom")
@EnableScheduling
public class BomApplication {
    public static void main(String[] args) {
        SpringApplication.run(BomApplication.class, args);
    }
}
