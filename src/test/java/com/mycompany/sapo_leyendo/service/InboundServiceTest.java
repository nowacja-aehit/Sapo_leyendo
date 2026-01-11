package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @InjectMocks
    private InboundService inboundService;

    @Mock
    private InboundOrderRepository inboundOrderRepository;

    @Mock
    private InboundOrderItemRepository inboundOrderItemRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private MoveTaskRepository moveTaskRepository;

    @Mock
    private DockAppointmentRepository dockAppointmentRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    private InboundOrder testOrder;
    private InboundOrderItem testItem;
    private Location dockLocation;
    private Location shelfLocation;
    private Product testProduct;
    private UnitOfMeasure testUom;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setSku("PROD-001");
        testProduct.setName("Test Product");
        testProduct.setIdBaseUom(1);

        testUom = new UnitOfMeasure();
        testUom.setId(1);
        testUom.setCode("SZT");
        testUom.setName("Sztuki");

        testOrder = new InboundOrder();
        testOrder.setId(1);
        testOrder.setReferenceNumber("INB-001");
        testOrder.setStatus("PLANNED");
        testOrder.setDockId(10);

        testItem = new InboundOrderItem();
        testItem.setId(1);
        testItem.setInboundOrder(testOrder);
        testItem.setProduct(testProduct);
        testItem.setQuantityExpected(100);
        testItem.setQuantityReceived(0);
        testItem.setBatchNumber("BATCH-001");

        dockLocation = new Location();
        dockLocation.setId(10);
        dockLocation.setName("DOCK-1");
        dockLocation.setActive(true);

        shelfLocation = new Location();
        shelfLocation.setId(20);
        shelfLocation.setName("A-01-01");
        shelfLocation.setActive(true);
        LocationType shelfType = new LocationType();
        shelfType.setName("SHELF");
        shelfLocation.setLocationType(shelfType);
    }

    @Test
    void getAllInboundOrders_shouldReturnAllOrders() {
        List<InboundOrder> orders = Arrays.asList(testOrder, new InboundOrder());
        when(inboundOrderRepository.findAll()).thenReturn(orders);

        List<InboundOrder> result = inboundService.getAllInboundOrders();

        assertEquals(2, result.size());
        verify(inboundOrderRepository).findAll();
    }

    @Test
    void getInboundOrderById_shouldReturnOrder_whenExists() {
        when(inboundOrderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        Optional<InboundOrder> result = inboundService.getInboundOrderById(1);

        assertTrue(result.isPresent());
        assertEquals("INB-001", result.get().getReferenceNumber());
    }

    @Test
    void getInboundOrderById_shouldReturnEmpty_whenNotExists() {
        when(inboundOrderRepository.findById(999)).thenReturn(Optional.empty());

        Optional<InboundOrder> result = inboundService.getInboundOrderById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void saveInboundOrder_shouldSaveAndReturnOrder() {
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenReturn(testOrder);

        InboundOrder result = inboundService.saveInboundOrder(testOrder);

        assertNotNull(result);
        assertEquals("INB-001", result.getReferenceNumber());
        verify(inboundOrderRepository).save(testOrder);
    }

    @Test
    void scheduleDock_shouldCreateAppointment() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        when(inboundOrderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(dockAppointmentRepository.save(any(DockAppointment.class))).thenAnswer(invocation -> {
            DockAppointment apt = invocation.getArgument(0);
            apt.setId(1);
            return apt;
        });

        DockAppointment result = inboundService.scheduleDock(1, 10, startTime, endTime, "Test Carrier");

        assertNotNull(result);
        assertEquals(testOrder, result.getInboundOrder());
        assertEquals(10, result.getDockId());
        assertEquals("Test Carrier", result.getCarrierName());
        verify(dockAppointmentRepository).save(any(DockAppointment.class));
    }

    @Test
    void scheduleDock_shouldThrowException_whenOrderNotFound() {
        when(inboundOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            inboundService.scheduleDock(999, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Carrier"));
    }

    @Test
    void generateLpn_shouldReturnUniqueLpn() {
        String lpn1 = inboundService.generateLpn();
        String lpn2 = inboundService.generateLpn();

        assertNotNull(lpn1);
        assertNotNull(lpn2);
        assertTrue(lpn1.startsWith("LPN-"));
        assertTrue(lpn2.startsWith("LPN-"));
        assertNotEquals(lpn1, lpn2); // Should be unique
    }

    @Test
    void receiveItem_shouldCreateReceiptAndInventory() {
        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.of(dockLocation));
        when(unitOfMeasureRepository.findById(1)).thenReturn(Optional.of(testUom));
        when(locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")).thenReturn(Optional.of(shelfLocation));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> {
            Receipt r = invocation.getArgument(0);
            r.setId(1);
            return r;
        });
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            inv.setId(1);
            return inv;
        });
        when(moveTaskRepository.save(any(MoveTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receipt result = inboundService.receiveItem(1, "LPN-001", 50, 1L, null);

        assertNotNull(result);
        assertEquals("LPN-001", result.getLpn());
        assertEquals(50, result.getQuantity());

        verify(receiptRepository).save(any(Receipt.class));
        verify(inventoryRepository).save(any(Inventory.class));
        verify(moveTaskRepository).save(any(MoveTask.class));
        verify(inboundOrderItemRepository).save(argThat(item -> item.getQuantityReceived() == 50));
    }

    @Test
    void receiveItem_shouldSetUomFromProduct() {
        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.of(dockLocation));
        when(unitOfMeasureRepository.findById(1)).thenReturn(Optional.of(testUom));
        when(locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")).thenReturn(Optional.of(shelfLocation));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(moveTaskRepository.save(any(MoveTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inboundService.receiveItem(1, "LPN-001", 50, 1L, null);

        verify(inventoryRepository).save(argThat(inv -> inv.getUom() != null && inv.getUom().getCode().equals("SZT")));
    }

    @Test
    void receiveItem_shouldUpdateOrderStatusToInProgress() {
        testOrder.setStatus("PLANNED");

        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.of(dockLocation));
        when(unitOfMeasureRepository.findById(1)).thenReturn(Optional.of(testUom));
        when(locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")).thenReturn(Optional.of(shelfLocation));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(moveTaskRepository.save(any(MoveTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inboundService.receiveItem(1, "LPN-001", 50, 1L, null);

        verify(inboundOrderRepository).save(argThat(order -> "IN_PROGRESS".equals(order.getStatus())));
    }

    @Test
    void receiveItem_shouldThrowException_whenItemNotFound() {
        when(inboundOrderItemRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            inboundService.receiveItem(999, "LPN-001", 50, 1L, null));
    }

    @Test
    void receiveItem_shouldThrowException_whenDockLocationNotFound() {
        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            inboundService.receiveItem(1, "LPN-001", 50, 1L, null));
    }

    @Test
    void receiveItem_shouldCreatePutAwayTask() {
        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.of(dockLocation));
        when(unitOfMeasureRepository.findById(1)).thenReturn(Optional.of(testUom));
        when(locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")).thenReturn(Optional.of(shelfLocation));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(moveTaskRepository.save(any(MoveTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inboundService.receiveItem(1, "LPN-001", 50, 1L, null);

        verify(moveTaskRepository).save(argThat(task -> 
            task.getType() == MoveTaskType.PUTAWAY &&
            task.getSourceLocation().equals(dockLocation) &&
            task.getTargetLocation().equals(shelfLocation) &&
            task.getStatus() == MoveTaskStatus.PENDING
        ));
    }

    @Test
    void receiveItem_shouldHandleDamageCode() {
        when(inboundOrderItemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(locationRepository.findById(10)).thenReturn(Optional.of(dockLocation));
        when(unitOfMeasureRepository.findById(1)).thenReturn(Optional.of(testUom));
        when(locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")).thenReturn(Optional.of(shelfLocation));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(moveTaskRepository.save(any(MoveTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receipt result = inboundService.receiveItem(1, "LPN-001", 50, 1L, "DAMAGED_CARTON");

        verify(receiptRepository).save(argThat(receipt -> "DAMAGED_CARTON".equals(receipt.getDamageCode())));
    }
}
