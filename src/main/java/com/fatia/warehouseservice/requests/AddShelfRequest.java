package com.fatia.warehouseservice.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddShelfRequest {
    private int originX;
    private int originY;
    private int width;
    private int length;
    private int height;
    private int level;
    private String description;
    private Map<String, String> conditions;
}
