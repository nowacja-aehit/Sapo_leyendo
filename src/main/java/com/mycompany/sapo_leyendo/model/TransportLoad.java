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

    @Column(name = "load_number", unique = true, nullable = false)
    private String loadNumber;

    @ManyToOne
    @JoinColumn(name = "id_carrier")
    private Carrier carrier;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoadStatus status = LoadStatus.PLANNING;

    @Column(name = "trailer_number")
    private String trailerNumber;

    @Column(name = "seal_number")
    private String sealNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "transportLoad")
    private List<Shipment> shipments;
}
