package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickingServiceTest {

    @InjectMocks
    private PickingService pickingService;

    @Mock
    private WaveRepository waveRepository;

    @Mock
    private OutboundOrderRepository outboundOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PickListRepository pickListRepository;

    @Mock
    private PickingTaskRepository pickingTaskRepository;

    @Test
    void testAllocateWave_Success() {
        // Arrange
        UUID waveId = UUID.randomUUID();
        Wave wave = new Wave();
        wave.setId(waveId);
        wave.setStatus(WaveStatus.PLANNED);

        Integer orderId = 1;
        OutboundOrder order = new OutboundOrder();
        order.setId(orderId);
        
        Product product = new Product();
        product.setId(1);
        product.setSku("SKU1");
        product.setName("Test Product");
        product.setIdCategory(1);
        product.setIdBaseUom(1);

        OutboundOrderItem item = new OutboundOrderItem();
        item.setProduct(product);
        item.setQuantityOrdered(10);
        order.setItems(List.of(item));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(20);
        inventory.setStatus(InventoryStatus.AVAILABLE);
        inventory.setLocation(new Location());

        when(waveRepository.findById(waveId)).thenReturn(Optional.of(wave));
        when(outboundOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(product.getId())).thenReturn(List.of(inventory));

        // Act
        pickingService.allocateWave(waveId, List.of(orderId));

        // Assert
        assertEquals(WaveStatus.ALLOCATED, wave.getStatus());
        verify(inventoryRepository, times(2)).save(any(Inventory.class)); // 1 update original, 1 create allocated
    }

    @Test
    void testAllocateWave_Shortage() {
        // Arrange
        UUID waveId = UUID.randomUUID();
        Wave wave = new Wave();
        wave.setId(waveId);
        wave.setStatus(WaveStatus.PLANNED);

        Integer orderId = 1;
        OutboundOrder order = new OutboundOrder();
        order.setId(orderId);
        
        Product product = new Product();
        product.setId(1);
        product.setSku("SKU1");
        product.setName("Test Product");
        product.setIdCategory(1);
        product.setIdBaseUom(1);

        OutboundOrderItem item = new OutboundOrderItem();
        item.setProduct(product);
        item.setQuantityOrdered(10);
        order.setItems(List.of(item));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(5); // Only 5 available
        inventory.setStatus(InventoryStatus.AVAILABLE);
        inventory.setLocation(new Location());

        when(waveRepository.findById(waveId)).thenReturn(Optional.of(wave));
        when(outboundOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(product.getId())).thenReturn(List.of(inventory));

        // Act
        pickingService.allocateWave(waveId, List.of(orderId));

        // Assert
        assertEquals(WaveStatus.ALLOCATED, wave.getStatus());
        verify(inventoryRepository, times(1)).save(any(Inventory.class)); // Only 1 save (status change to ALLOCATED)
        // In real scenario we would check logs or return value for shortage
    }
}
