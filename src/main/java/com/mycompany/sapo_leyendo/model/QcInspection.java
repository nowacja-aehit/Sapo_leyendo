package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "QcInspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QcInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inspection")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private InspectionSourceType sourceType;

    @Column(name = "reference_id")
    private Integer referenceId; // e.g., ReceiptLine ID or ReturnItem ID

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "id_test_plan")
    private TestPlan testPlan;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Enumerated(EnumType.STRING)
    private InspectionResult result;

    @Column(name = "inspector_id")
    private Integer inspectorId; // User ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
