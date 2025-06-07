package com.fatia.warehouseservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "warehouse")
@Getter
@Setter
public class WarehouseConfig {
    private int width;
    private int height;
    private int originX;
    private int originY;
}
