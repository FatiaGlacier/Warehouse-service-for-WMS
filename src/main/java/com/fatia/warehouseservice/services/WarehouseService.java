package com.fatia.warehouseservice.services;

import com.fatia.warehouseservice.config.WarehouseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseConfig warehouseConfig;

    public boolean isWithinBounds(int x, int y) {
        return x >= warehouseConfig.getOriginX()
                && y >= warehouseConfig.getOriginY()
                && x <= warehouseConfig.getWidth()
                && y <= warehouseConfig.getHeight();
    }

    public boolean isWithinBounds(int originX, int originY, int width, int height) {
        return originX >= warehouseConfig.getOriginX()
                && originY >= warehouseConfig.getOriginY()
                && (originX + width) <= warehouseConfig.getWidth()
                && (originY + height) <= warehouseConfig.getHeight();
    }
}
