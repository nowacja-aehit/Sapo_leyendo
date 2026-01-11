package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Entity
@Table(name = "Inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventory")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "id_location", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "id_uom", nullable = false)
    private UnitOfMeasure uom;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "lpn")
    private String lpn;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(name = "unit_price")
    private java.math.BigDecimal unitPrice; // Override product price for this specific inventory
    
    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @JsonProperty("id")
    public String getExternalId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("name")
    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    @JsonProperty("sku")
    public String getSku() {
        return product != null ? product.getSku() : null;
    }

    @JsonProperty("category")
    public String getCategoryName() {
        return product != null ? product.getCategoryName() : null;
    }

    @JsonProperty("location")
    public String getLocationCode() {
        return location != null ? location.getName() : null;
    }

    @JsonProperty("status")
    public String getStatusLabel() {
        if (quantity == null || quantity == 0) {
            return "Out of Stock";
        }
        if (Optional.ofNullable(reorderLevel).orElse(0) >= quantity) {
            return "Low Stock";
        }
        return "In Stock";
    }

    @JsonProperty("price")
    public java.math.BigDecimal getPriceValue() {
        // Return inventory-specific price if set, otherwise use product price
        if (unitPrice != null) {
            return unitPrice;
        }
        return product != null ? product.getPriceValue() : null;
    }

    public void setUnitPrice(java.math.BigDecimal price) {
        this.unitPrice = price;
    }

    @JsonProperty("reorderLevel")
    public Integer getExternalReorderLevel() {
        return reorderLevel != null ? reorderLevel : Optional.ofNullable(product)
                .map(Product::getMinStockLevel).orElse(0);
    }

    @JsonProperty("lastUpdated")
    public String getLastUpdated() {
        if (receivedAt == null) {
            return LocalDate.now().toString();
        }
        return receivedAt.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
