package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "OutboundOrders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outbound_order")
    private Integer id;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "status", nullable = false)
    private String status; // PLANNED, PICKED, SHIPPED, CANCELLED

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "destination")
    private String destination;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL)
    private List<OutboundOrderItem> items;
}
