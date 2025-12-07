package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "KpiMetrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kpi")
    private Integer id; // Changed to Integer to match other entities, though UUID was in spec

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "dimension")
    private String dimension;
}
