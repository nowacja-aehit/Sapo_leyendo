package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "InboundOrders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inbound_order")
    private Integer id;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "status", nullable = false)
    private String status; // PLANNED, RECEIVED, CANCELLED

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "dock_id")
    private Integer dockId;
    
    @OneToMany(mappedBy = "inboundOrder", cascade = CascadeType.ALL)
    private List<InboundOrderItem> items;
}
