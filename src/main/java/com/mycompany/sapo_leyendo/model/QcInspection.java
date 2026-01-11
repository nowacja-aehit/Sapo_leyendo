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
    @Column(name = "id_qc_inspection")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private InspectionSourceType sourceType;

    @Column(name = "source_id")
    private Integer referenceId; // e.g., ReceiptLine ID or ReturnItem ID

    @ManyToOne
    @JoinColumn(name = "id_product")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "id_inventory")
    private Inventory inventory;

    @ManyToOne
    @JoinColumn(name = "id_test_plan")
    private TestPlan testPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private InspectionResult result = InspectionResult.PENDING;

    @Column(name = "inspector_notes")
    private String inspectorNotes;

    @Column(name = "id_user_inspector")
    private Integer inspectorId; // User ID

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
