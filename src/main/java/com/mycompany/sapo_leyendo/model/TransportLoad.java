package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TransportLoads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_load")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_carrier", nullable = false)
    private Carrier carrier;

    @Column(name = "vehicle_plate_number")
    private String vehiclePlateNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Enumerated(EnumType.STRING)
    private LoadStatus status;

    @Column(name = "dock_id")
    private Integer dockId;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;
    
    @OneToMany(mappedBy = "transportLoad")
    private List<Shipment> shipments;
}
