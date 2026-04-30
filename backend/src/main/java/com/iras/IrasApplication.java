package com.iras;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.iras.mapper")
public class IrasApplication {
    public static void main(String[] args) {
        SpringApplication.run(IrasApplication.class, args);
    }
}
