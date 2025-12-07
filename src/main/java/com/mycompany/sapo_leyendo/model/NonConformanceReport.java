package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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

    @OneToOne
    @JoinColumn(name = "id_inspection", nullable = false)
    private QcInspection inspection;

    @Enumerated(EnumType.STRING)
    @Column(name = "defect_type")
    private DefectType defectType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "NcrPhotos", joinColumns = @JoinColumn(name = "id_ncr"))
    @Column(name = "photo_url")
    private List<String> photos;

    @Column(name = "vendor_response", columnDefinition = "TEXT")
    private String vendorResponse;
}
