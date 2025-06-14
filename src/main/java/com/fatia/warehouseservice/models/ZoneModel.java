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
    private String uuid;
    private String name;
    private String type;
    private int originX;
    private int originY;
    private int width;
    private int length;
    private int rotationAngle;
    private int faceDirection;
    private String description;
    private List<ShelfModel> shelves;
    private String connectedNodeId;
    private Long parentZoneId;
    private List<ZoneModel> childZones;

    public static ZoneModel toModel(ZoneEntity entity) {
        return ZoneModel
                .builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .name(entity.getName())
                .type(entity.getType().name())
                .originX(entity.getOriginX())
                .originY(entity.getOriginY())
                .width(entity.getWidth())
                .length(entity.getLength())
                .description(entity.getDescription())
                .rotationAngle(entity.getRotationAngle())
                .faceDirection(entity.getFaceDirection().ordinal())
                .shelves(
                        entity.getShelves()
                                .stream()
                                .map(ShelfModel::toModel)
                                .collect(Collectors.toList())
                )
                .parentZoneId(entity.getParentZone().getId())
                .childZones(
                        entity.getChildZones()
                                .stream()
                                .map(ZoneModel::toModel)
                                .collect(Collectors.toList())
                )
                .connectedNodeId(entity.getNodeId())
                .build();
    }
}
