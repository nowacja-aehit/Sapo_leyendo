package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QualityControlService {

    @Autowired
    private QcInspectionRepository qcInspectionRepository;

    @Autowired
    private TestPlanRepository testPlanRepository;

    @Autowired
    private NonConformanceReportRepository ncrRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public QcInspection createInspection(Integer productId, InspectionSourceType sourceType, Integer referenceId, Integer sampleSize) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        QcInspection inspection = new QcInspection();
        inspection.setProduct(product);
        inspection.setSourceType(sourceType);
        inspection.setReferenceId(referenceId);
        inspection.setResult(InspectionResult.PENDING);
        inspection.setInspectorNotes(sampleSize != null ? "Sample size: " + sampleSize : null);
        inspection.setCreatedAt(LocalDateTime.now());
        
        // Try to find a default test plan (simplified logic)
        // In a real app, we might look up by product category or specific assignment
        List<TestPlan> plans = testPlanRepository.findAll();
        if (!plans.isEmpty()) {
            inspection.setTestPlan(plans.get(0));
        }

        return qcInspectionRepository.save(inspection);
    }

    @Transactional
    public QcInspection executeInspection(Integer inspectionId, InspectionResult result, Integer inspectorId) {
        QcInspection inspection = qcInspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));

        inspection.setResult(result);
        inspection.setInspectorId(inspectorId);
        inspection.setInspectedAt(LocalDateTime.now());

        return qcInspectionRepository.save(inspection);
    }

    @Transactional
    public NonConformanceReport createNcr(Integer inspectionId, DefectType defectType, String description, List<String> photos) {
        QcInspection inspection = qcInspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));

        if (inspection.getResult() == InspectionResult.PASSED) {
            throw new RuntimeException("Cannot create NCR for a passed inspection");
        }

        NonConformanceReport ncr = new NonConformanceReport();
        ncr.setNcrNumber("NCR-" + System.currentTimeMillis());
        ncr.setInspection(inspection);
        ncr.setDefectType(defectType);
        ncr.setSeverity(NcrSeverity.MINOR); // Default severity
        ncr.setDescription(description);
        ncr.setStatus(NcrStatus.OPEN);
        ncr.setCreatedAt(LocalDateTime.now());

        return ncrRepository.save(ncr);
    }
    
    @Transactional
    public TestPlan createTestPlan(String name, String description, String testSteps) {
        TestPlan plan = new TestPlan();
        plan.setName(name);
        plan.setDescription(description);
        plan.setTestSteps(testSteps);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        return testPlanRepository.save(plan);
    }

    /**
     * Calculates sample size based on simplified ISO 2859-1 (General Inspection Level II)
     * This is a mock implementation of the standard table.
     */
    public int calculateSampleSize(int lotSize) {
        if (lotSize <= 0) return 0;
        if (lotSize <= 8) return 2;
        if (lotSize <= 15) return 3;
        if (lotSize <= 25) return 5;
        if (lotSize <= 50) return 8;
        if (lotSize <= 90) return 13;
        if (lotSize <= 150) return 20;
        if (lotSize <= 280) return 32;
        if (lotSize <= 500) return 50;
        if (lotSize <= 1200) return 80;
        if (lotSize <= 3200) return 125;
        if (lotSize <= 10000) return 200;
        return 315; // > 10000
    }

    public boolean shouldInspect(Integer productId, Integer supplierId) {
        // Logic to determine if inspection is needed
        // 1. Check if product is flagged for QC
        // 2. Check if supplier is flagged
        // 3. Random sampling (e.g. 10%)
        
        // Mock implementation
        return Math.random() < 0.1; // 10% chance
    }
}
