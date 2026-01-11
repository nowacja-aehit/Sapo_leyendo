package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InboundService {

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private InboundOrderItemRepository inboundOrderItemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private MoveTaskRepository moveTaskRepository;

    @Autowired
    private DockAppointmentRepository dockAppointmentRepository;

    @Autowired
    private UnitOfMeasureRepository unitOfMeasureRepository;

    public List<InboundOrder> getAllInboundOrders() {
        return inboundOrderRepository.findAll();
    }

    public Optional<InboundOrder> getInboundOrderById(Integer id) {
        return inboundOrderRepository.findById(id);
    }

    public InboundOrder saveInboundOrder(InboundOrder order) {
        // Auto-generate referenceNumber if not provided (required field)
        if (order.getReferenceNumber() == null || order.getReferenceNumber().isEmpty()) {
            order.setReferenceNumber("INB-" + System.currentTimeMillis());
        }
        return inboundOrderRepository.save(order);
    }

    @Transactional
    public DockAppointment scheduleDock(Integer inboundOrderId, Integer dockId, LocalDateTime startTime, LocalDateTime endTime, String carrierName) {
        InboundOrder order = inboundOrderRepository.findById(inboundOrderId)
                .orElseThrow(() -> new RuntimeException("Inbound Order not found"));

        // Basic validation: Check for overlapping appointments on the same dock
        // This is a simplified check. In production, we'd use a custom query.
        // For now, we just save it.
        
        DockAppointment appointment = new DockAppointment();
        appointment.setInboundOrder(order);
        appointment.setDockId(dockId);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setCarrierName(carrierName);
        
        return dockAppointmentRepository.save(appointment);
    }

    public String generateLpn() {
        // Simple LPN generation logic: "LPN-" + Timestamp + Random
        return "LPN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    @Transactional
    public Receipt receiveItem(Integer inboundOrderItemId, String lpn, Integer quantity, Long operatorId, String damageCode) {
        InboundOrderItem item = inboundOrderItemRepository.findById(inboundOrderItemId)
                .orElseThrow(() -> new RuntimeException("Inbound Order Item not found"));

        Receipt receipt = new Receipt();
        receipt.setInboundOrderItem(item);
        receipt.setLpn(lpn);
        receipt.setQuantity(quantity);
        receipt.setOperatorId(operatorId);
        receipt.setTimestamp(LocalDateTime.now());
        receipt.setDamageCode(damageCode);

        receiptRepository.save(receipt);

        // Create Inventory at Dock
        InboundOrder order = item.getInboundOrder();
        Location dockLocation;
        if (order.getDockId() != null) {
            dockLocation = locationRepository.findById(order.getDockId())
                    .orElseThrow(() -> new RuntimeException("Dock location not found"));
        } else {
            // Use first available DOCK location as default
            dockLocation = locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("DOCK")
                    .orElseGet(() -> locationRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No locations available")));
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(item.getProduct());
        inventory.setLocation(dockLocation);
        inventory.setQuantity(quantity);
        inventory.setLpn(lpn);
        inventory.setBatchNumber(item.getBatchNumber());
        inventory.setStatus(InventoryStatus.AVAILABLE);
        inventory.setReceivedAt(LocalDateTime.now()); // Required field
        
        // Set UOM - use product's base UOM or find default "SZT" (pieces)
        UnitOfMeasure uom = null;
        if (item.getProduct() != null && item.getProduct().getIdBaseUom() != null) {
            uom = unitOfMeasureRepository.findById(item.getProduct().getIdBaseUom()).orElse(null);
        }
        if (uom == null) {
            uom = unitOfMeasureRepository.findById(1).orElse(null); // Default to first UOM
        }
        inventory.setUom(uom);
        
        inventoryRepository.save(inventory);

        // Create PutAway Task
        MoveTask task = new MoveTask();
        task.setType(MoveTaskType.PUTAWAY);
        task.setInventory(inventory);
        task.setSourceLocation(dockLocation);
        
        // Find Target Location (Simple Strategy: First available SHELF)
        Location targetLocation = locationRepository.findFirstByLocationTypeNameAndIsActiveTrue("SHELF")
                .orElse(null);
        task.setTargetLocation(targetLocation);

        task.setPriority(5);
        task.setStatus(MoveTaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        moveTaskRepository.save(task);

        item.setQuantityReceived(item.getQuantityReceived() + quantity);
        inboundOrderItemRepository.save(item);

        if ("PLANNED".equals(order.getStatus()) || "ARRIVED".equals(order.getStatus())) {
            order.setStatus("IN_PROGRESS");
            inboundOrderRepository.save(order);
        }

        return receipt;
    }
}
