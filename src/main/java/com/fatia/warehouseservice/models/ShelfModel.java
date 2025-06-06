package com.fatia.warehouseservice.models;

import com.fatia.warehouseservice.entities.ShelfEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShelfModel {
    private Long id;
    private String name;
    private String zoneName;
    private int originX;
    private int originY;
    private int width;
    private int length;
    private int height;
    private int level;
    private String description;
    private boolean isOccupied;
    private Map<String, String> conditions;

    public static ShelfModel toModel(ShelfEntity entity) {
        return ShelfModel
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .zoneName(entity.getZone().getName())
                .originX(entity.getOriginX())
                .originY(entity.getOriginY())
                .width(entity.getWidth())
                .length(entity.getLength())
                .height(entity.getHeight())
                .level(entity.getLevel())
                .description(entity.getDescription())
                .isOccupied(entity.isOccupied())
                .conditions(entity.getConditions())
                .build();
    }
}
