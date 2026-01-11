package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.service.PackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackingController using mocks.
 * Tests controller logic without requiring full database integration.
 */
@ExtendWith(MockitoExtension.class)
class PackingControllerTest {

    @Mock
    private PackingService packingService;

    @InjectMocks
    private PackingController packingController;

    private OutboundOrder testOrder;
    private Shipment testShipment;
    private Parcel testParcel;
    private PackingMaterial testMaterial;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test outbound order
        testOrder = new OutboundOrder();
        testOrder.setId(1);
        testOrder.setReferenceNumber("TEST-PACK-001");
        testOrder.setStatus("NEW");
        testOrder.setDestination("Test Destination");
        testOrder.setCreatedAt(LocalDateTime.now());

        // Create test shipment
        testShipment = new Shipment();
        testShipment.setId(100);
        testShipment.setStatus(ShipmentStatus.PACKING);
        testShipment.setOutboundOrder(testOrder);
        testShipment.setParcels(new ArrayList<>());

        // Create test packing material
        testMaterial = new PackingMaterial();
        testMaterial.setId(10);
        testMaterial.setCode("BOX-TEST");
        testMaterial.setLengthCm(30.0);
        testMaterial.setWidthCm(20.0);
        testMaterial.setHeightCm(15.0);
        testMaterial.setTareWeightKg(0.5);
        testMaterial.setMaxWeightKg(10.0);

        // Create test parcel
        testParcel = new Parcel();
        testParcel.setId(50);
        testParcel.setShipment(testShipment);
        testParcel.setPackingMaterial(testMaterial);
        testParcel.setWeightKg(0.5);
        testParcel.setItems(new ArrayList<>());

        // Create test product
        testProduct = new Product();
        testProduct.setId(5);
        testProduct.setSku("TEST-SKU-PACK");
        testProduct.setName("Test Packing Product");
        testProduct.setWeightKg(1.5);
    }

    @Test
    void shouldStartPackingAndCreateShipment() {
        when(packingService.startPacking(1)).thenReturn(testShipment);

        ResponseEntity<Shipment> response = packingController.startPacking(1);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(ShipmentStatus.PACKING, response.getBody().getStatus());
        assertEquals(100, response.getBody().getId());
        verify(packingService, times(1)).startPacking(1);
    }

    @Test
    void shouldReturnExistingShipmentWhenStartPackingTwice() {
        // Service should return existing shipment (idempotent)
        when(packingService.startPacking(1)).thenReturn(testShipment);

        ResponseEntity<Shipment> first = packingController.startPacking(1);
        ResponseEntity<Shipment> second = packingController.startPacking(1);

        assertEquals(first.getBody().getId(), second.getBody().getId());
        verify(packingService, times(2)).startPacking(1);
    }

    @Test
    void shouldCreateParcelWithPackingMaterial() {
        when(packingService.createParcel(100, 10)).thenReturn(testParcel);

        ResponseEntity<Parcel> response = packingController.createParcel(100, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().getId());
        assertEquals(0.5, response.getBody().getWeightKg());
        verify(packingService, times(1)).createParcel(100, 10);
    }

    @Test
    void shouldAddItemToParcel() {
        // After adding item, parcel weight increases
        testParcel.setWeightKg(3.5); // 0.5 tare + 1.5*2 items
        when(packingService.addItemToParcel(50, 5, 2)).thenReturn(testParcel);

        ResponseEntity<Parcel> response = packingController.addItemToParcel(50, 5, 2);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().getId());
        assertEquals(3.5, response.getBody().getWeightKg());
        verify(packingService, times(1)).addItemToParcel(50, 5, 2);
    }

    @Test
    void shouldCloseShipmentAndUpdateStatus() {
        Shipment closedShipment = new Shipment();
        closedShipment.setId(100);
        closedShipment.setStatus(ShipmentStatus.PACKED);
        closedShipment.setTotalWeightKg(5.0);

        when(packingService.closeShipment(100)).thenReturn(closedShipment);

        ResponseEntity<Shipment> response = packingController.closeShipment(100);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(ShipmentStatus.PACKED, response.getBody().getStatus());
        assertEquals(5.0, response.getBody().getTotalWeightKg());
        verify(packingService, times(1)).closeShipment(100);
    }

    @Test
    void shouldThrowExceptionWhenOutboundOrderNotFound() {
        when(packingService.startPacking(999)).thenThrow(new RuntimeException("Outbound Order not found"));

        assertThrows(RuntimeException.class, () -> packingController.startPacking(999));
        verify(packingService, times(1)).startPacking(999);
    }

    @Test
    void shouldThrowExceptionWhenShipmentNotFoundForClose() {
        when(packingService.closeShipment(999)).thenThrow(new RuntimeException("Shipment not found"));

        assertThrows(RuntimeException.class, () -> packingController.closeShipment(999));
        verify(packingService, times(1)).closeShipment(999);
    }

    @Test
    void shouldHandleEmptyParcelsGracefully() {
        Shipment emptyShipment = new Shipment();
        emptyShipment.setId(100);
        emptyShipment.setStatus(ShipmentStatus.PACKED);
        emptyShipment.setTotalWeightKg(0.0);
        emptyShipment.setParcels(new ArrayList<>());

        when(packingService.closeShipment(100)).thenReturn(emptyShipment);

        ResponseEntity<Shipment> response = packingController.closeShipment(100);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ShipmentStatus.PACKED, response.getBody().getStatus());
        assertEquals(0.0, response.getBody().getTotalWeightKg());
    }

    @Test
    void shouldHandleNullParcelsGracefully() {
        Shipment nullParcelsShipment = new Shipment();
        nullParcelsShipment.setId(100);
        nullParcelsShipment.setStatus(ShipmentStatus.PACKED);
        nullParcelsShipment.setTotalWeightKg(0.0);
        nullParcelsShipment.setParcels(null);

        when(packingService.closeShipment(100)).thenReturn(nullParcelsShipment);

        ResponseEntity<Shipment> response = packingController.closeShipment(100);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldAddMultipleItemsToParcel() {
        ParcelItem item1 = new ParcelItem();
        item1.setProduct(testProduct);
        item1.setQuantity(2);

        Product product2 = new Product();
        product2.setId(6);
        product2.setWeightKg(2.0);

        ParcelItem item2 = new ParcelItem();
        item2.setProduct(product2);
        item2.setQuantity(1);

        List<ParcelItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        testParcel.setItems(items);
        testParcel.setWeightKg(5.5); // 0.5 + 1.5*2 + 2.0*1

        when(packingService.addItemToParcel(50, 5, 2)).thenReturn(testParcel);

        ResponseEntity<Parcel> response = packingController.addItemToParcel(50, 5, 2);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(5.5, response.getBody().getWeightKg());
    }

    @Test
    void shouldHandleParcelWithNullItems() {
        testParcel.setItems(null);
        when(packingService.addItemToParcel(50, 5, 1)).thenReturn(testParcel);

        ResponseEntity<Parcel> response = packingController.addItemToParcel(50, 5, 1);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
