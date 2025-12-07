package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ParcelItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parcel_item")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_parcel", nullable = false)
    @JsonIgnore
    private Parcel parcel;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
