package com.fatia.warehouseservice.models;

import com.fatia.warehouseservice.entities.ZoneEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZoneModel {
    private Long id;
    private String name;
    private String type;
    private int originX;
    private int originY;
    private int width; // X
    private int height; // Y
    private String description;
    private List<ShelfModel> shelves;

    public static ZoneModel toModel(ZoneEntity entity) {
        return ZoneModel
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .originX(entity.getOriginX())
                .originY(entity.getOriginY())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .description(entity.getDescription())
                .shelves(
                        entity.getShelves()
                                .stream()
                                .map(ShelfModel::toModel)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
