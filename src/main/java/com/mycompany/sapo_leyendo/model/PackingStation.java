package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PackingStations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_packing_station")
    private Integer id;

    private String name;
    private String printerIp;
    private String scaleIp;

    @Enumerated(EnumType.STRING)
    private PackingStationStatus status;
}
