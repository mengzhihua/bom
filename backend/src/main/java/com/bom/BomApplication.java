package com.bom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Paths;

@SpringBootApplication
@MapperScan("com.bom")
@EnableScheduling
public class BomApplication {
    public static void main(String[] args) {
        if (System.getProperty("bom.data.dir") == null
                && Boolean.getBoolean("bom.native")) {
            System.setProperty("bom.data.dir", Paths.get(
                    System.getProperty("user.home"), ".bom", "data")
                    .toString());
        }
        SpringApplication.run(BomApplication.class, args);
    }
}
