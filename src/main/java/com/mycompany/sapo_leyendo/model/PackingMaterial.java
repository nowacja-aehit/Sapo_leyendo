package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PackingMaterials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_packing_material")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    private Double widthCm;
    private Double heightCm;
    private Double lengthCm;
    
    private Double tareWeightKg;
    private Double maxWeightKg;
}
