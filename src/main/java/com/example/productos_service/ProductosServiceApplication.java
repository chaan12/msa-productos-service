package com.example.productos_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ProductosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }

}
