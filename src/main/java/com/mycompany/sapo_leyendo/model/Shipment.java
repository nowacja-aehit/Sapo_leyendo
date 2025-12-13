package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_shipment")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_outbound_order", nullable = false)
    private OutboundOrder outboundOrder;

    @Column(name = "id_carrier")
    private Integer carrierId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "shipped_at")
    private java.time.LocalDateTime shippedAt;

     @ManyToOne
     @JoinColumn(name = "id_load")
     private TransportLoad transportLoad;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

     @Column(name = "total_weight_kg")
     private Double totalWeightKg;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<Parcel> parcels;
}
