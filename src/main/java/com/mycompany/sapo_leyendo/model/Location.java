package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_location")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "A-01-01"

    @Column(name = "zone")
    private String zone; // e.g., "Zone A"

    @Column(name = "type")
    private String type; // e.g., "SHELF", "FLOOR"

    @Column(name = "is_active")
    private boolean isActive = true;
}
