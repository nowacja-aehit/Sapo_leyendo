package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.QcInspectionRepository;
import com.mycompany.sapo_leyendo.service.QualityControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qc")
public class QualityControlController {

    @Autowired
    private QualityControlService qcService;

    @Autowired
    private QcInspectionRepository qcInspectionRepository;

    /**
     * Get all QC inspections - for dashboard/list view
     */
    @GetMapping("/inspections")
    public ResponseEntity<List<QcInspection>> getAllInspections() {
        return ResponseEntity.ok(qcInspectionRepository.findAll());
    }

    @PostMapping("/inspections")
    public ResponseEntity<QcInspection> createInspection(
            @RequestParam Integer productId,
            @RequestParam InspectionSourceType sourceType,
            @RequestParam Integer referenceId,
            @RequestParam Integer sampleSize) {
        return ResponseEntity.ok(qcService.createInspection(productId, sourceType, referenceId, sampleSize));
    }

    @PostMapping("/inspections/{inspectionId}/execute")
    public ResponseEntity<QcInspection> executeInspection(
            @PathVariable Integer inspectionId,
            @RequestParam InspectionResult result,
            @RequestParam Integer inspectorId) {
        return ResponseEntity.ok(qcService.executeInspection(inspectionId, result, inspectorId));
    }

    @PostMapping("/inspections/{inspectionId}/ncr")
    public ResponseEntity<NonConformanceReport> createNcr(
            @PathVariable Integer inspectionId,
            @RequestParam String defectType,
            @RequestParam String description,
            @RequestBody(required = false) List<String> photos) {
        // Parse defectType string to enum
        DefectType type;
        try {
            type = DefectType.valueOf(defectType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If parsing fails, default to OTHER
            type = DefectType.OTHER;
        }
        return ResponseEntity.ok(qcService.createNcr(inspectionId, type, description, photos));
    }
    
    @PostMapping("/test-plans")
    public ResponseEntity<TestPlan> createTestPlan(@RequestBody TestPlan testPlan) {
        return ResponseEntity.ok(qcService.createTestPlan(testPlan.getName(), testPlan.getDescription(), testPlan.getTestSteps()));
    }
}
