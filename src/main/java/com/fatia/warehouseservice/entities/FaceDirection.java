package com.fatia.warehouseservice.entities;

public enum FaceDirection {
    UP(0),
    RIGHT(90),
    BOTTOM(180),
    LEFT(270);

    private final int angle;

    FaceDirection(int angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }

    public static FaceDirection fromString(String direction) {
        return switch (direction.toLowerCase()) {
            case "up", "north" -> UP;
            case "right", "east" -> RIGHT;
            case "down", "bottom", "south" -> BOTTOM;
            case "left", "west" -> LEFT;
            default -> throw new IllegalArgumentException("Unknown direction: " + direction);
        };
    }

    public static int getAngleFromString(String direction) {
        return fromString(direction).getAngle();
    }
}
