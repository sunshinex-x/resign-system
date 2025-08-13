package com.example.resign;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.resign.mapper")
public class ResignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResignApplication.class, args);
    }

}