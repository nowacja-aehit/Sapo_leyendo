package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Manifests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_manifest")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_load", nullable = false)
    private TransportLoad transportLoad;

    @Column(name = "carrier_manifest_id")
    private String carrierManifestId;

    @Column(name = "total_parcels")
    private Integer totalParcels;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "generation_date")
    private LocalDateTime generationDate;
}
