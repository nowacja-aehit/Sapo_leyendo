package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingController - tests shipping management:
 * - Carrier operations
 * - Shipment CRUD
 * - Transport load operations
 * - Dispatch and manifest generation
 */
@ExtendWith(MockitoExtension.class)
class ShippingControllerTest {

    @Mock
    private ShippingService shippingService;

    @InjectMocks
    private ShippingController shippingController;

    private Carrier testCarrier;
    private Shipment testShipment;
    private TransportLoad testLoad;
    private Manifest testManifest;
    private OutboundOrder testOrder;

    @BeforeEach
    void setUp() {
        // Create test carrier
        testCarrier = new Carrier();
        testCarrier.setId(1);
        testCarrier.setName("Test Carrier");
        testCarrier.setTrackingUrlTemplate("https://track.test.com/{tracking}");

        // Create test outbound order
        testOrder = new OutboundOrder();
        testOrder.setId(1);
        testOrder.setReferenceNumber("OUT-SHIP-001");
        testOrder.setStatus("PACKED");

        // Create test shipment - Shipment uses carrierId (Integer), not carrier object
        testShipment = new Shipment();
        testShipment.setId(1);
        testShipment.setOutboundOrder(testOrder);
        testShipment.setCarrierId(testCarrier.getId());
        testShipment.setStatus(ShipmentStatus.PACKING);
        testShipment.setTrackingNumber("TRACK-001");

        // Create test load
        testLoad = new TransportLoad();
        testLoad.setId(1);
        testLoad.setCarrier(testCarrier);
        testLoad.setLoadNumber("LOAD-001");
        testLoad.setTrailerNumber("DW12345");
        testLoad.setDriverName("Jan Kierowca");
        testLoad.setDriverPhone("123456789");
        testLoad.setStatus(LoadStatus.PLANNING);

        // Create test manifest
        testManifest = new Manifest();
        testManifest.setId(1);
        testManifest.setTransportLoad(testLoad);
        testManifest.setManifestNumber("MAN-001");
    }

    // ==================== Carrier Tests ====================

    @Test
    void shouldGetAllCarriers() {
        List<Carrier> carriers = Arrays.asList(testCarrier);
        when(shippingService.getAllCarriers()).thenReturn(carriers);

        List<Carrier> result = shippingController.getAllCarriers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Carrier", result.get(0).getName());
        verify(shippingService, times(1)).getAllCarriers();
    }

    @Test
    void shouldReturnEmptyCarrierList() {
        when(shippingService.getAllCarriers()).thenReturn(new ArrayList<>());

        List<Carrier> result = shippingController.getAllCarriers();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetCarrierById() {
        when(shippingService.getCarrierById(1)).thenReturn(Optional.of(testCarrier));

        ResponseEntity<Carrier> response = shippingController.getCarrierById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Test Carrier", response.getBody().getName());
    }

    @Test
    void shouldReturn404WhenCarrierNotFound() {
        when(shippingService.getCarrierById(999)).thenReturn(Optional.empty());

        ResponseEntity<Carrier> response = shippingController.getCarrierById(999);

        assertEquals(404, response.getStatusCodeValue());
    }

    // ==================== Shipment Tests ====================

    @Test
    void shouldGetAllShipments() {
        List<Shipment> shipments = Arrays.asList(testShipment);
        when(shippingService.getAllShipments()).thenReturn(shipments);

        List<Shipment> result = shippingController.getAllShipments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TRACK-001", result.get(0).getTrackingNumber());
        verify(shippingService, times(1)).getAllShipments();
    }

    @Test
    void shouldReturnEmptyShipmentList() {
        when(shippingService.getAllShipments()).thenReturn(new ArrayList<>());

        List<Shipment> result = shippingController.getAllShipments();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetShipmentById() {
        when(shippingService.getShipmentById(1)).thenReturn(Optional.of(testShipment));

        ResponseEntity<Shipment> response = shippingController.getShipmentById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(ShipmentStatus.PACKING, response.getBody().getStatus());
    }

    @Test
    void shouldReturn404WhenShipmentNotFound() {
        when(shippingService.getShipmentById(999)).thenReturn(Optional.empty());

        ResponseEntity<Shipment> response = shippingController.getShipmentById(999);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldCreateShipment() {
        when(shippingService.createShipment(1, 1, "TRACK-NEW"))
                .thenReturn(testShipment);

        ResponseEntity<Shipment> response = shippingController.createShipment(1, 1, "TRACK-NEW");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(shippingService, times(1)).createShipment(1, 1, "TRACK-NEW");
    }

    @Test
    void shouldCreateShipmentWithoutTrackingNumber() {
        Shipment shipmentNoTracking = new Shipment();
        shipmentNoTracking.setId(2);
        shipmentNoTracking.setTrackingNumber(null);

        when(shippingService.createShipment(1, 1, null)).thenReturn(shipmentNoTracking);

        ResponseEntity<Shipment> response = shippingController.createShipment(1, 1, null);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody().getTrackingNumber());
    }

    @Test
    void shouldUpdateShipment() {
        Shipment updatedShipment = new Shipment();
        updatedShipment.setId(1);
        updatedShipment.setStatus(ShipmentStatus.SHIPPED);
        updatedShipment.setTrackingNumber("TRACK-UPDATED");

        when(shippingService.updateShipment(eq(1), any(Shipment.class)))
                .thenReturn(Optional.of(updatedShipment));

        ResponseEntity<Shipment> response = shippingController.updateShipment(1, updatedShipment);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ShipmentStatus.SHIPPED, response.getBody().getStatus());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentShipment() {
        Shipment updateRequest = new Shipment();
        when(shippingService.updateShipment(eq(999), any(Shipment.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<Shipment> response = shippingController.updateShipment(999, updateRequest);

        assertEquals(404, response.getStatusCodeValue());
    }

    // ==================== Transport Load Tests ====================

    @Test
    void shouldGetAllLoads() {
        List<TransportLoad> loads = Arrays.asList(testLoad);
        when(shippingService.getAllLoads()).thenReturn(loads);

        List<TransportLoad> result = shippingController.getAllLoads();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DW12345", result.get(0).getTrailerNumber());
    }

    @Test
    void shouldCreateLoad() {
        when(shippingService.createLoad(1, "DW12345", "Jan", "123"))
                .thenReturn(testLoad);

        ResponseEntity<TransportLoad> response = shippingController.createLoad(
                1, "DW12345", "Jan", "123");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(LoadStatus.PLANNING, response.getBody().getStatus());
        verify(shippingService, times(1)).createLoad(1, "DW12345", "Jan", "123");
    }

    @Test
    void shouldAssignShipmentToLoad() {
        doNothing().when(shippingService).assignShipmentToLoad(1, 1);

        ResponseEntity<Void> response = shippingController.assignShipmentToLoad(1, 1);

        assertEquals(200, response.getStatusCodeValue());
        verify(shippingService, times(1)).assignShipmentToLoad(1, 1);
    }

    @Test
    void shouldThrowExceptionWhenAssigningToDispatchedLoad() {
        doThrow(new RuntimeException("Cannot assign to a dispatched load"))
                .when(shippingService).assignShipmentToLoad(1, 1);

        assertThrows(RuntimeException.class, () -> 
                shippingController.assignShipmentToLoad(1, 1));
    }

    @Test
    void shouldDispatchLoad() {
        testLoad.setStatus(LoadStatus.IN_TRANSIT);
        when(shippingService.dispatchLoad(1)).thenReturn(testManifest);

        ResponseEntity<Manifest> response = shippingController.dispatchLoad(1);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("MAN-001", response.getBody().getManifestNumber());
        verify(shippingService, times(1)).dispatchLoad(1);
    }

    @Test
    void shouldThrowExceptionWhenDispatchingNonExistentLoad() {
        when(shippingService.dispatchLoad(999))
                .thenThrow(new RuntimeException("Load not found"));

        assertThrows(RuntimeException.class, () -> 
                shippingController.dispatchLoad(999));
    }

    // ==================== Full Workflow Tests ====================

    @Test
    void shouldCompleteFullShippingWorkflow() {
        // Step 1: Create load
        when(shippingService.createLoad(1, "DW12345", "Jan", "123"))
                .thenReturn(testLoad);
        ResponseEntity<TransportLoad> loadResponse = shippingController.createLoad(
                1, "DW12345", "Jan", "123");
        assertEquals(LoadStatus.PLANNING, loadResponse.getBody().getStatus());

        // Step 2: Create shipment
        when(shippingService.createShipment(1, 1, "TRACK-001"))
                .thenReturn(testShipment);
        ResponseEntity<Shipment> shipmentResponse = shippingController.createShipment(1, 1, "TRACK-001");
        assertNotNull(shipmentResponse.getBody());

        // Step 3: Assign shipment to load
        doNothing().when(shippingService).assignShipmentToLoad(1, 1);
        ResponseEntity<Void> assignResponse = shippingController.assignShipmentToLoad(1, 1);
        assertEquals(200, assignResponse.getStatusCodeValue());

        // Step 4: Dispatch load and generate manifest
        when(shippingService.dispatchLoad(1)).thenReturn(testManifest);
        ResponseEntity<Manifest> manifestResponse = shippingController.dispatchLoad(1);
        assertNotNull(manifestResponse.getBody());

        verify(shippingService, times(1)).createLoad(anyInt(), anyString(), anyString(), anyString());
        verify(shippingService, times(1)).createShipment(anyInt(), anyInt(), anyString());
        verify(shippingService, times(1)).assignShipmentToLoad(anyInt(), anyInt());
        verify(shippingService, times(1)).dispatchLoad(anyInt());
    }

    @Test
    void shouldHandleMultipleShipmentsPerLoad() {
        // Create multiple shipments
        Shipment shipment1 = new Shipment();
        shipment1.setId(1);
        Shipment shipment2 = new Shipment();
        shipment2.setId(2);
        Shipment shipment3 = new Shipment();
        shipment3.setId(3);

        when(shippingService.createShipment(anyInt(), anyInt(), anyString()))
                .thenReturn(shipment1, shipment2, shipment3);

        // Create 3 shipments
        shippingController.createShipment(1, 1, "T1");
        shippingController.createShipment(2, 1, "T2");
        shippingController.createShipment(3, 1, "T3");

        // Assign all to same load
        doNothing().when(shippingService).assignShipmentToLoad(anyInt(), anyInt());
        shippingController.assignShipmentToLoad(1, 1);
        shippingController.assignShipmentToLoad(1, 2);
        shippingController.assignShipmentToLoad(1, 3);

        verify(shippingService, times(3)).createShipment(anyInt(), anyInt(), anyString());
        verify(shippingService, times(3)).assignShipmentToLoad(anyInt(), anyInt());
    }

    @Test
    void shouldGetShipmentWithFullDetails() {
        testShipment.setOutboundOrder(testOrder);
        testShipment.setCarrierId(testCarrier.getId());
        testShipment.setTotalWeightKg(15.5);

        when(shippingService.getShipmentById(1)).thenReturn(Optional.of(testShipment));

        ResponseEntity<Shipment> response = shippingController.getShipmentById(1);

        assertNotNull(response.getBody().getOutboundOrder());
        assertNotNull(response.getBody().getCarrierId());
        assertEquals(15.5, response.getBody().getTotalWeightKg());
    }
}
