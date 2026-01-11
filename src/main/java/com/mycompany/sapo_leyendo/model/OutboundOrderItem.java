package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "OutboundOrderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outbound_order_item")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_outbound_order", nullable = false)
    @JsonIgnore
    private OutboundOrder outboundOrder;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    // Setter to handle productId from frontend JSON
    @com.fasterxml.jackson.annotation.JsonSetter("productId")
    public void setProductId(Integer productId) {
        if (productId != null) {
            this.product = new Product();
            this.product.setId(productId);
        }
    }

    @ManyToOne
    @JoinColumn(name = "id_uom", nullable = false)
    private UnitOfMeasure uom;

    @Column(name = "quantity_ordered", nullable = false)
    private Double quantityOrdered;

    @Column(name = "quantity_picked", nullable = false)
    private Double quantityPicked = 0.0;

    @Column(name = "quantity_shipped", nullable = false)
    private Double quantityShipped = 0.0;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "location_code")
    private String locationCode;

    @Column(name = "status")
    private String status;

    @JsonProperty("ordered")
    public Double getOrderedQuantity() {
        return quantityOrdered;
    }

    @JsonProperty("picked")
    public Double getPickedQuantity() {
        return quantityPicked;
    }

    @JsonProperty("shipped")
    public Double getShippedQuantity() {
        return quantityShipped;
    }

    @JsonProperty("price")
    public BigDecimal getPriceValue() {
        return unitPrice;
    }

    @JsonProperty("total")
    public BigDecimal getLineTotalValue() {
        if (lineTotal != null) {
            return lineTotal;
        }
        if (unitPrice == null || quantityOrdered == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
    }

    @JsonProperty("product")
    public String getItemProductName() {
        if (productName != null) {
            return productName;
        }
        return product != null ? product.getName() : "Produkt";
    }

    @JsonProperty("sku")
    public String getItemSku() {
        if (sku != null) {
            return sku;
        }
        return product != null ? product.getSku() : "SKU";
    }

    @JsonProperty("location")
    public String getItemLocation() {
        return locationCode != null ? locationCode : "MAGA-01";
    }

    @JsonProperty("status")
    public String getItemStatus() {
        if (status != null) {
            return status;
        }
        if (quantityShipped != null && quantityOrdered != null) {
            if (quantityShipped == 0) {
                return "Zaplanowany";
            }
            if (quantityShipped < quantityOrdered) {
                return "Czesciowo wyslany";
            }
            return "Wyslany";
        }
        return "Zaplanowany";
    }
}
