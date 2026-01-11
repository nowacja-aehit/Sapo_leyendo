package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
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
    @JsonProperty("orderReference")
    private String referenceNumber;

    @Column(name = "status", nullable = false)
    private String status = "PLANNED"; // PLANNED, RECEIVED, CANCELLED

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "expected_date")
    @JsonProperty("expectedArrival")
    private LocalDateTime expectedDate;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "dock_id")
    private Integer dockId;
    
    @OneToMany(mappedBy = "inboundOrder", cascade = CascadeType.ALL)
    private List<InboundOrderItem> items;
}
