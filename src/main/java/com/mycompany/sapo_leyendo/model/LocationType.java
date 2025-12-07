package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LocationTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_location_type")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "max_volume")
    private Double maxVolume;

    private Double length;
    private Double width;
    private Double height;
}
