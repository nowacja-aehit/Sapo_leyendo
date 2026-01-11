package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "InboundOrderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inbound_order_item")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_inbound_order", nullable = false)
    @JsonIgnore
    private InboundOrder inboundOrder;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(name = "quantity_expected", nullable = false)
    private Integer quantityExpected;

    @JsonProperty("receivedQuantity")
    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived = 0;

    @Column(name = "batch_number")
    private String batchNumber;

    // Transient field for JSON deserialization (not persisted)
    @Transient
    private Integer productId;

    // Alias setter for JSON - accepts both "quantity" and "expectedQuantity"
    @JsonProperty("quantity")
    public void setQuantity(Integer quantity) {
        this.quantityExpected = quantity;
    }

    @JsonProperty("expectedQuantity")
    public Integer getExpectedQuantity() {
        return quantityExpected;
    }

    @JsonProperty("expectedQuantity")
    public void setExpectedQuantity(Integer quantity) {
        this.quantityExpected = quantity;
    }

    @JsonProperty("productName")
    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    @JsonProperty("sku")
    public String getSku() {
        return product != null ? product.getSku() : null;
    }

    public Integer getProductId() {
        if (productId != null) return productId;
        return product != null ? product.getId() : null;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}