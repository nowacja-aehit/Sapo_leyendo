package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
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

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "shipped_at")
    private java.time.LocalDateTime shippedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_load")
    @JsonIgnore
    private TransportLoad transportLoad;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

     @Column(name = "total_weight_kg")
     private Double totalWeightKg;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<Parcel> parcels;
}
