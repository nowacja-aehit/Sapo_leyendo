package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category")
    @JsonIgnore
    private ProductCategory category;

    @Column(name = "id_base_uom", nullable = false)
    private Integer idBaseUom;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @JsonProperty("category")
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    @JsonProperty("price")
    public BigDecimal getPriceValue() {
        return unitPrice;
    }
}
