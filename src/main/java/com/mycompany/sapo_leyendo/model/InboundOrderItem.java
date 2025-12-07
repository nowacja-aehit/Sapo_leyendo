package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived = 0;

    @Column(name = "batch_number")
    private String batchNumber;
}
