package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Entity
@Table(name = "Carriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrier")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "service_type")
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type")
    private IntegrationType integrationType;

    @Column(name = "cutoff_time")
    private LocalTime cutoffTime;
    
    @Column(name = "tracking_url_template")
    private String trackingUrlTemplate;
}
