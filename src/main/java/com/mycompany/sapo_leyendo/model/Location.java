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

    @ManyToOne
    @JoinColumn(name = "id_zone")
    private Zone zone;

    @ManyToOne
    @JoinColumn(name = "id_location_type")
    private LocationType locationType;

    private String barcode;
    private String aisle;
    private String rack;
    private String level;
    private String bin;

    @Column(name = "pick_sequence")
    private Integer pickSequence;

    @Enumerated(EnumType.STRING)
    private LocationStatus status = LocationStatus.ACTIVE;

    @Column(name = "is_active")
    private boolean isActive = true;
}
