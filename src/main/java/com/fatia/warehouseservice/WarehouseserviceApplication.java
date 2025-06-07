package com.fatia.warehouseservice;

import com.fatia.warehouseservice.config.WarehouseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WarehouseConfig.class)
public class WarehouseserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseserviceApplication.class, args);
    }

}
