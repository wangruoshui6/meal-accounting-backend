package com.accounting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.accounting.mapper")
public class MealAccountingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MealAccountingBackendApplication.class, args);
    }

}

