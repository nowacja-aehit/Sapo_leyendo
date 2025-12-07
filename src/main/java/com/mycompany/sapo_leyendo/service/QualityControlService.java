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
        inspection.setSampleSize(sampleSize);
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
        inspection.setCompletedAt(LocalDateTime.now());

        return qcInspectionRepository.save(inspection);
    }

    @Transactional
    public NonConformanceReport createNcr(Integer inspectionId, DefectType defectType, String description, List<String> photos) {
        QcInspection inspection = qcInspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));

        if (inspection.getResult() == InspectionResult.PASS) {
            throw new RuntimeException("Cannot create NCR for a passed inspection");
        }

        NonConformanceReport ncr = new NonConformanceReport();
        ncr.setInspection(inspection);
        ncr.setDefectType(defectType);
        ncr.setDescription(description);
        ncr.setPhotos(photos);

        return ncrRepository.save(ncr);
    }
    
    @Transactional
    public TestPlan createTestPlan(String name, Double aqlLevel, List<TestStep> steps) {
        TestPlan plan = new TestPlan();
        plan.setName(name);
        plan.setAqlLevel(aqlLevel);
        plan.setSteps(steps);
        return testPlanRepository.save(plan);
    }
}
