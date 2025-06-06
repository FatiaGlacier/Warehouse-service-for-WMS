package com.fatia.warehouseservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "zones")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ZoneEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String type;

    private int originX;

    private int originY;

    private int width; // X

    private int height; // Y

    private String description;

    @OneToMany(
            mappedBy = "zone",
            cascade = CascadeType.ALL)
    private List<ShelfEntity> shelves;
}
