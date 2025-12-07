package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "quantity_picked", nullable = false)
    private Integer quantityPicked = 0;
}
