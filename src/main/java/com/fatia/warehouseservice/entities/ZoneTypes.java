package com.fatia.warehouseservice.entities;

import java.util.Arrays;
import java.util.List;

public enum ZoneTypes {
    WAREHOUSE,
    STORAGE,
    COLUMN,
    LOADING,
    UNLOADING,
    PARKING,
    PARKING_SPOT;

    public List<ZoneTypes> getAllowedChildren() {
        return switch (this) {
            case STORAGE -> List.of(COLUMN);
            case PARKING -> List.of(PARKING_SPOT);
            case WAREHOUSE -> List.of(STORAGE, PARKING, UNLOADING, LOADING);
            default -> List.of();
        };
    }

    public List<ZoneTypes> getAllowedParents() {
        return switch (this) {
            case COLUMN -> List.of(STORAGE);
            case PARKING_SPOT -> List.of(PARKING);
            default -> List.of();
        };
    }

    public static List<ZoneTypes> getWarehouseChildrenZones() {
        return List.of(STORAGE, PARKING, UNLOADING, LOADING);
    }

    //For zone types validation
    public static boolean isValidZoneType(String type) {
        return Arrays.stream(ZoneTypes.values())
                .anyMatch(z -> z.name().equalsIgnoreCase(type));
    }
}
