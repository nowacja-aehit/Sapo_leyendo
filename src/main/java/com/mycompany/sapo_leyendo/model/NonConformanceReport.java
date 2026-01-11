package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "NonConformanceReports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonConformanceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ncr")
    private Integer id;

    @Column(name = "ncr_number", unique = true, nullable = false)
    private String ncrNumber;

    @ManyToOne
    @JoinColumn(name = "id_qc_inspection")
    private QcInspection inspection;

    @Enumerated(EnumType.STRING)
    @Column(name = "defect_type", nullable = false)
    private DefectType defectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private NcrSeverity severity = NcrSeverity.MINOR;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition")
    private NcrDisposition disposition;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NcrStatus status = NcrStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
