package com.bookstore.cartorder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class CartOrderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(CartOrderServiceApplication.class, args); }
}
