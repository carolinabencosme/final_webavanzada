package com.hospedaje.cartorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "room_units",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_room_unit_code", columnNames = {"unit_code"}),
        @UniqueConstraint(name = "uk_room_unit_property_type_order", columnNames = {"property_id", "room_type", "unit_order"})
    },
    indexes = {
        @Index(name = "idx_room_unit_property_type", columnList = "property_id, room_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private String propertyId;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(name = "unit_code", nullable = false)
    private String unitCode;

    @Column(name = "unit_order", nullable = false)
    private Integer unitOrder;
}
