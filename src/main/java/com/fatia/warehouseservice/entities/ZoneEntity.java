package com.fatia.warehouseservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @Column(unique = true, nullable = false)
    private String uuid;

    private String name;

    @Enumerated(EnumType.STRING)
    private ZoneType type;

    // origin X relatively to parent
    // top-left point
    private int originX;

    // origin Y relatively to parent
    // top-left point
    private int originY;

    private int width; // shortest side

    private int length; // longest side

    //angle for rotating on map
    @Column(nullable = false)
    private Integer rotationAngle;// 0 90 180 270

    //direction for entry node
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaceDirection faceDirection;

    @Column(name = "node_id")
    private String nodeId; // entry node to zone

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_zone_id")
    private ZoneEntity parentZone;

    @OneToMany(mappedBy = "parentZone", cascade = CascadeType.ALL)
    private List<ZoneEntity> childZones = new ArrayList<>();

    @OneToMany(
            mappedBy = "zone",
            cascade = CascadeType.ALL)
    private List<ShelfEntity> shelves;

    private String description;
}
