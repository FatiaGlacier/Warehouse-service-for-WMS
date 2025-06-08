package com.fatia.warehouseservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "shelves")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ShelfEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private ZoneEntity zone;// COLUMN zone

    private int originX;

    private int originY;

    private int width;// X

    private int length;// Y

    private int height;// Z

    private int level;

    private String description;

    private boolean isOccupied;

    private boolean isActive;

    @Column(name = "connected_node_id")
    private String connectedNodeId;

    @Type(io.hypersistence.utils.hibernate.type.json.JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            columnDefinition = "jsonb")
    private Map<String, String> conditions;
}
