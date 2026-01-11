package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.InventoryRepository;
import com.mycompany.sapo_leyendo.repository.LocationRepository;
import com.mycompany.sapo_leyendo.repository.MoveTaskRepository;
import com.mycompany.sapo_leyendo.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MoveTaskRepository moveTaskRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private com.mycompany.sapo_leyendo.repository.StockCountSessionRepository stockCountSessionRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoryByProduct(Integer productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> getInventoryByLocation(Integer locationId) {
        return inventoryRepository.findByLocationId(locationId);
    }

    public Optional<Inventory> getInventoryById(Integer id) {
        return inventoryRepository.findById(id);
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Integer id) {
        inventoryRepository.deleteById(id);
    }

    // Put-away Logic
    @Transactional
    public MoveTask createPutAwayTask(Integer receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        // 1. Create Inventory record (initially in Staging/Dock)
        // Assuming we have a default "RECEIVING" location or we use the dock from InboundOrder
        // For simplicity, let's assume the receipt implies the item is at a "DOCK" location.
        // We need to find a target location.

        // Simple strategy: Find first active location of type 'SHELF' that is empty or has same product
        // This is a placeholder for the complex algorithm described in spec.
        Location targetLocation = locationRepository.findAll().stream()
                .filter(l -> l.isActive() && "SHELF".equals(l.getLocationType() != null ? l.getLocationType().getName() : ""))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No suitable location found for put-away"));

        // Create Inventory record (Virtual move to system)
        Inventory inventory = new Inventory();
        inventory.setProduct(receipt.getInboundOrderItem().getProduct());
        inventory.setQuantity(receipt.getQuantity());
        inventory.setLpn(receipt.getLpn());
        inventory.setStatus(InventoryStatus.AVAILABLE); // Or QC_HOLD if QC module says so
        // Ideally, source location is the Dock.
        // For now, we set location to target directly? No, that defeats the purpose of a task.
        // Let's say current location is null or a special "RECEIVING" location.
        // But Inventory entity requires a location.
        // Let's fetch a "DOCK" location.
        Location dockLocation = locationRepository.findAll().stream()
                .filter(l -> "DOCK".equals(l.getLocationType() != null ? l.getLocationType().getName() : ""))
                .findFirst()
                .orElse(targetLocation); // Fallback

        inventory.setLocation(dockLocation);
        inventory = inventoryRepository.save(inventory);

        // 2. Create Move Task
        MoveTask task = new MoveTask();
        task.setType(MoveTaskType.PUTAWAY);
        task.setInventory(inventory);
        task.setSourceLocation(dockLocation);
        task.setTargetLocation(targetLocation);
        task.setPriority(5);
        task.setStatus(MoveTaskStatus.PENDING);

        return moveTaskRepository.save(task);
    }

    public List<MoveTask> getPendingTasks() {
        return moveTaskRepository.findByStatus(MoveTaskStatus.PENDING);
    }

    @Transactional
    public MoveTask createManualMoveTask(Integer inventoryId, Integer targetLocationId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        Location targetLocation = locationRepository.findById(targetLocationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        MoveTask task = new MoveTask();
        task.setType(MoveTaskType.RELOCATE);
        task.setInventory(inventory);
        task.setSourceLocation(inventory.getLocation());
        task.setTargetLocation(targetLocation);
        task.setPriority(10);
        task.setStatus(MoveTaskStatus.PENDING);
        
        return moveTaskRepository.save(task);
    }

    @Transactional
    public void completeTask(Integer taskId) {
        MoveTask task = moveTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setStatus(MoveTaskStatus.COMPLETED);
        moveTaskRepository.save(task);
        
        // Update inventory location
        Inventory inventory = task.getInventory();
        inventory.setLocation(task.getTargetLocation());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public StockCountSession createStockCountSession(StockCountType type, List<Integer> locationIds) {
        StockCountSession session = new StockCountSession();
        session.setType(type);
        session.setStatus(StockCountStatus.OPEN);
        
        List<Location> locations = locationRepository.findAllById(locationIds);
        session.setLocations(locations);
        
        return stockCountSessionRepository.save(session);
    }
}
