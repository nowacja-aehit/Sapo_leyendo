package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QualityControlServiceTest {

    @InjectMocks
    private QualityControlService qcService;

    @Mock
    private QcInspectionRepository qcInspectionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TestPlanRepository testPlanRepository;

    @Mock
    private NonConformanceReportRepository ncrRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateSampleSize() {
        // ISO 2859-1 General Inspection Level II (Simplified)
        assertEquals(2, qcService.calculateSampleSize(5));
        assertEquals(3, qcService.calculateSampleSize(10));
        assertEquals(5, qcService.calculateSampleSize(20));
        assertEquals(8, qcService.calculateSampleSize(40));
        assertEquals(13, qcService.calculateSampleSize(80));
        assertEquals(20, qcService.calculateSampleSize(100));
        assertEquals(32, qcService.calculateSampleSize(200));
    }

    @Test
    void testCreateInspection() {
        // Arrange
        Integer productId = 1;
        Product product = new Product();
        product.setId(1);
        product.setSku("SKU1");
        product.setName("Test Product");
        ProductCategory category = new ProductCategory();
        category.setId(1);
        product.setCategory(category);
        product.setIdBaseUom(1);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(qcInspectionRepository.save(any(QcInspection.class))).thenAnswer(invocation -> {
            QcInspection inspection = invocation.getArgument(0);
            inspection.setId(1);
            return inspection;
        });

        // Act
        QcInspection result = qcService.createInspection(productId, InspectionSourceType.INBOUND, 100, 20);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProduct().getId());
        assertEquals(InspectionSourceType.INBOUND, result.getSourceType());
        assertEquals(20, result.getSampleSize());
        verify(qcInspectionRepository).save(any(QcInspection.class));
    }
}
