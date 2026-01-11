package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackingServiceTest {

    @InjectMocks
    private PackingService packingService;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OutboundOrderRepository outboundOrderRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelItemRepository parcelItemRepository;

    @Mock
    private PackingMaterialRepository packingMaterialRepository;

    @Mock
    private ProductRepository productRepository;

    private OutboundOrder testOrder;
    private Shipment testShipment;
    private PackingMaterial testMaterial;
    private Product testProduct;
    private Parcel testParcel;

    @BeforeEach
    void setUp() {
        testOrder = new OutboundOrder();
        testOrder.setId(1);
        testOrder.setReferenceNumber("ORD-001");
        testOrder.setStatus("PICKED");

        testShipment = new Shipment();
        testShipment.setId(1);
        testShipment.setOutboundOrder(testOrder);
        testShipment.setStatus(ShipmentStatus.PACKING);
        testShipment.setTotalWeightKg(0.0);
        testShipment.setParcels(new ArrayList<>());

        testMaterial = new PackingMaterial();
        testMaterial.setId(1);
        testMaterial.setCode("BOX-SM");
        testMaterial.setTareWeightKg(0.3);
        testMaterial.setMaxWeightKg(5.0);

        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setSku("PROD-001");
        testProduct.setName("Test Product");
        testProduct.setWeightKg(1.5);

        testParcel = new Parcel();
        testParcel.setId(1);
        testParcel.setShipment(testShipment);
        testParcel.setPackingMaterial(testMaterial);
        testParcel.setWeightKg(0.3);
        testParcel.setItems(new ArrayList<>());
    }

    @Test
    void startPacking_shouldCreateNewShipment_whenNoExistingShipment() {
        when(outboundOrderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(shipmentRepository.findByOutboundOrderId(1)).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setId(1);
            return s;
        });

        Shipment result = packingService.startPacking(1);

        assertNotNull(result);
        assertEquals(ShipmentStatus.PACKING, result.getStatus());
        assertEquals(testOrder, result.getOutboundOrder());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void startPacking_shouldReturnExistingShipment_whenAlreadyExists() {
        when(outboundOrderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(shipmentRepository.findByOutboundOrderId(1)).thenReturn(Optional.of(testShipment));

        Shipment result = packingService.startPacking(1);

        assertNotNull(result);
        assertEquals(testShipment.getId(), result.getId());
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void startPacking_shouldThrowException_whenOrderNotFound() {
        when(outboundOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packingService.startPacking(999));
    }

    @Test
    void createParcel_shouldCreateParcelWithTareWeight() {
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(packingMaterialRepository.findById(1)).thenReturn(Optional.of(testMaterial));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> {
            Parcel p = invocation.getArgument(0);
            p.setId(1);
            return p;
        });

        Parcel result = packingService.createParcel(1, 1);

        assertNotNull(result);
        assertEquals(testMaterial.getTareWeightKg(), result.getWeightKg());
        assertEquals(testShipment, result.getShipment());
        assertEquals(testMaterial, result.getPackingMaterial());
    }

    @Test
    void createParcel_shouldThrowException_whenShipmentNotFound() {
        when(shipmentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packingService.createParcel(999, 1));
    }

    @Test
    void createParcel_shouldThrowException_whenMaterialNotFound() {
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(packingMaterialRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packingService.createParcel(1, 999));
    }

    @Test
    void addItemToParcel_shouldAddItemAndUpdateWeight() {
        testShipment.setParcels(List.of(testParcel));
        
        when(parcelRepository.findById(1)).thenReturn(Optional.of(testParcel));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(parcelItemRepository.save(any(ParcelItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcel result = packingService.addItemToParcel(1, 1, 2);

        assertNotNull(result);
        // Initial weight (0.3) + 2 items * 1.5kg = 3.3kg
        assertEquals(3.3, result.getWeightKg(), 0.01);
        verify(parcelItemRepository).save(any(ParcelItem.class));
    }

    @Test
    void addItemToParcel_shouldThrowException_whenParcelNotFound() {
        when(parcelRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packingService.addItemToParcel(999, 1, 1));
    }

    @Test
    void addItemToParcel_shouldThrowException_whenProductAlreadyPacked() {
        ParcelItem existingItem = new ParcelItem();
        existingItem.setProduct(testProduct);
        testParcel.setItems(List.of(existingItem));
        testShipment.setParcels(List.of(testParcel));

        when(parcelRepository.findById(1)).thenReturn(Optional.of(testParcel));

        assertThrows(IllegalStateException.class, () -> packingService.addItemToParcel(1, 1, 1));
    }

    @Test
    void addItemToParcel_shouldHandleNullProductWeight() {
        testProduct.setWeightKg(null);
        testShipment.setParcels(List.of(testParcel));
        
        when(parcelRepository.findById(1)).thenReturn(Optional.of(testParcel));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(parcelItemRepository.save(any(ParcelItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcel result = packingService.addItemToParcel(1, 1, 2);

        assertNotNull(result);
        // Weight should remain unchanged since product weight is null
        assertEquals(0.3, result.getWeightKg(), 0.01);
        verify(parcelRepository, never()).save(any(Parcel.class)); // Weight not updated
    }

    @Test
    void closeShipment_shouldUpdateStatusAndCalculateWeight() {
        testParcel.setWeightKg(5.0);
        Parcel parcel2 = new Parcel();
        parcel2.setId(2);
        parcel2.setWeightKg(3.0);
        testShipment.setParcels(List.of(testParcel, parcel2));

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = packingService.closeShipment(1);

        assertEquals(ShipmentStatus.PACKED, result.getStatus());
        assertEquals(8.0, result.getTotalWeightKg(), 0.01);
        assertEquals("PACKED", testOrder.getStatus());
    }

    @Test
    void closeShipment_shouldHandleEmptyParcels() {
        testShipment.setParcels(new ArrayList<>());

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = packingService.closeShipment(1);

        assertEquals(ShipmentStatus.PACKED, result.getStatus());
        assertEquals(0.0, result.getTotalWeightKg(), 0.01);
    }

    @Test
    void closeShipment_shouldHandleNullParcels() {
        testShipment.setParcels(null);

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = packingService.closeShipment(1);

        assertEquals(ShipmentStatus.PACKED, result.getStatus());
        assertEquals(0.0, result.getTotalWeightKg(), 0.01);
    }

    @Test
    void closeShipment_shouldHandleNullParcelWeight() {
        testParcel.setWeightKg(null);
        testShipment.setParcels(List.of(testParcel));

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = packingService.closeShipment(1);

        assertEquals(ShipmentStatus.PACKED, result.getStatus());
        assertEquals(0.0, result.getTotalWeightKg(), 0.01);
    }

    @Test
    void closeShipment_shouldThrowException_whenShipmentNotFound() {
        when(shipmentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packingService.closeShipment(999));
    }
}
