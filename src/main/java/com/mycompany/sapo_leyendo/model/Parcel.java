package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Parcels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parcel")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_shipment", nullable = false)
    @JsonIgnore
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "id_packing_material")
    private PackingMaterial packingMaterial;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "tracking_sub_number")
    private String trackingSubNumber;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    private List<ParcelItem> items;
}
