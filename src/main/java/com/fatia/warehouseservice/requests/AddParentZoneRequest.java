package com.fatia.warehouseservice.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddParentZoneRequest {
    private String type;
    private int originX;
    private int originY;
    private int width; // X
    private int length; // Y
    private int rotationAngle;
    private String faceDirection;
    private String description;
}
