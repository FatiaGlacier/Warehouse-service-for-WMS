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
    private ZoneTypes type;

    private int originX; // origin X relatively to parent

    private int originY; // origin Y relatively to parent

    private int width; // X

    private int height; // Y

    private String description;

    @Column(name = "connected_node_id")
    private String connectedNodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_zone_id")
    private ZoneEntity parentZone;

    @OneToMany(mappedBy = "parentZone", cascade = CascadeType.ALL)
    private List<ZoneEntity> childZones = new ArrayList<>();

    @OneToMany(
            mappedBy = "zone",
            cascade = CascadeType.ALL)
    private List<ShelfEntity> shelves;
}
