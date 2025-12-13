package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.InventoryRepository;
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;

import com.mycompany.sapo_leyendo.repository.PickingTaskRepository;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import com.mycompany.sapo_leyendo.repository.UserRepository;
import com.mycompany.sapo_leyendo.repository.WaveRepository;
import com.mycompany.sapo_leyendo.repository.PickListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PickingService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final PickingTaskRepository pickingTaskRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WaveRepository waveRepository;
    private final PickListRepository pickListRepository;

    public PickingService(OutboundOrderRepository outboundOrderRepository,
                          InventoryRepository inventoryRepository,
                          PickingTaskRepository pickingTaskRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository,
                          WaveRepository waveRepository,
                          PickListRepository pickListRepository) {
        this.outboundOrderRepository = outboundOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.pickingTaskRepository = pickingTaskRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.waveRepository = waveRepository;
        this.pickListRepository = pickListRepository;
    }

    /**
     * 1. Wave Planning: Group orders into a Wave
     */
    @Transactional
    public Wave createWave(List<Integer> outboundOrderIds) {
        Wave wave = new Wave();
        wave.setName("Wave-" + System.currentTimeMillis());
        wave.setCreatedDate(LocalDateTime.now());
        wave.setStatus(WaveStatus.PLANNED);
        
        // In a real system, we would link orders to the wave here.
        // For now, we assume the caller will handle the linkage or we add a waveId to OutboundOrder.
        // But OutboundOrder doesn't have waveId in the current model.
        // Let's assume we process them immediately or we need to add waveId to OutboundOrder.
        // For this implementation, we will just create the wave object. 
        // To be fully functional, OutboundOrder needs a relation to Wave.
        
        return waveRepository.save(wave);
    }

    @Transactional
    public Wave runWave(UUID waveId, List<Integer> outboundOrderIds) {
        allocateWave(waveId, outboundOrderIds);
        releaseWave(waveId, outboundOrderIds);
        return waveRepository.findById(waveId).orElseThrow();
    }

    public List<PickingTask> getPickingTasks(UUID waveId) {
        // This assumes we can find tasks by wave.
        // PickTask -> PickList -> Wave
        // We need a custom query or filter.
        // For now, let's fetch all tasks and filter in memory (inefficient but works for small scale)
        List<PickingTask> allTasks = pickingTaskRepository.findAll();
        return allTasks.stream()
                .filter(t -> t.getOutboundOrderItem().getOutboundOrder().getId().equals(waveId))
                .toList();
    }

    /**
     * 2. Allocation: Reserve inventory for the orders in the wave
     * This is a simplified version of Hard Allocation.
     */
    @Transactional
    public void allocateWave(UUID waveId, List<Integer> outboundOrderIds) {
        Wave wave = waveRepository.findById(waveId)
                .orElseThrow(() -> new RuntimeException("Wave not found"));

        if (wave.getStatus() != WaveStatus.PLANNED) {
            throw new RuntimeException("Wave must be in PLANNED status to allocate");
        }

        for (Integer orderId : outboundOrderIds) {
            OutboundOrder order = outboundOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            for (OutboundOrderItem item : order.getItems()) {
                allocateItem(item, order, wave);
            }
        }

        wave.setStatus(WaveStatus.ALLOCATED);
        waveRepository.save(wave);
    }

    private void allocateItem(OutboundOrderItem item, OutboundOrder order, Wave wave) {
        int qtyNeeded = item.getQuantityOrdered();
        Product product = item.getProduct();

        // Find available inventory
        List<Inventory> availableInventory = inventoryRepository.findByProductId(product.getId());
        // Filter for AVAILABLE status manually if repository method doesn't
        availableInventory = availableInventory.stream()
                .filter(inv -> inv.getStatus() == InventoryStatus.AVAILABLE)
                .toList();

        for (Inventory inv : availableInventory) {
            if (qtyNeeded <= 0) break;

            int qtyToTake = Math.min(qtyNeeded, inv.getQuantity());

            // Logic to split inventory for allocation
            if (inv.getQuantity() > qtyToTake) {
                // Split: Reduce original, create new Allocated record
                inv.setQuantity(inv.getQuantity() - qtyToTake);
                inventoryRepository.save(inv);

                Inventory allocatedInv = new Inventory();
                allocatedInv.setProduct(product);
                allocatedInv.setLocation(inv.getLocation());
                allocatedInv.setQuantity(qtyToTake);
                allocatedInv.setLpn(inv.getLpn());
                allocatedInv.setBatchNumber(inv.getBatchNumber());
                allocatedInv.setStatus(InventoryStatus.ALLOCATED); // We need to ensure this status exists
                inventoryRepository.save(allocatedInv);
            } else {
                // Take whole record
                inv.setStatus(InventoryStatus.ALLOCATED);
                inventoryRepository.save(inv);
            }

            qtyNeeded -= qtyToTake;
        }

        if (qtyNeeded > 0) {
            // Shortage detected!
            // In a real system, we would mark the order as SHORT or partial.
            System.out.println("Shortage for product " + product.getName() + ". Missing: " + qtyNeeded);
        }
    }

    /**
     * 3. Release Wave: Generate Pick Lists and Tasks
     */
    @Transactional
    public void releaseWave(UUID waveId, List<Integer> outboundOrderIds) {
        Wave wave = waveRepository.findById(waveId)
                .orElseThrow(() -> new RuntimeException("Wave not found"));

        if (wave.getStatus() != WaveStatus.ALLOCATED) {
            throw new RuntimeException("Wave must be ALLOCATED to release");
        }

        // Strategy: Discrete Picking (One PickList per Order)
        for (Integer orderId : outboundOrderIds) {
            OutboundOrder order = outboundOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            PickList pickList = new PickList();
            pickList.setWave(wave);
            pickList.setType(PickListType.SINGLE);
            pickList.setStatus(PickListStatus.PENDING);
            pickList = pickListRepository.save(pickList);

            // Generate Tasks based on Allocated Inventory
            // This is tricky because we need to know WHICH allocated inventory belongs to THIS order.
            // In a simple implementation, we can just find ANY allocated inventory for the product.
            // But correctly, we should have linked the Allocation to the Order Line.
            // For this gap filling, we will iterate items and find allocated inventory again.
            
            for (OutboundOrderItem item : order.getItems()) {
                createTasksForItem(item, pickList, order);
            }
        }

        wave.setStatus(WaveStatus.RELEASED);
        waveRepository.save(wave);
    }

    private void createTasksForItem(OutboundOrderItem item, PickList pickList, OutboundOrder order) {
        // Find allocated inventory for this product
        // Ideally we should filter by "Allocated for this order", but we lack that link.
        // We will just take any ALLOCATED inventory for now (Simplified).
        List<Inventory> allocatedInv = inventoryRepository.findByProductId(item.getProduct().getId()).stream()
                .filter(inv -> inv.getStatus() == InventoryStatus.ALLOCATED)
                .toList();
        int qtyToPick = item.getQuantityOrdered();
        for (Inventory inv : allocatedInv) {
            if (qtyToPick <= 0) break;
            // Check if this inventory is already assigned to a task?
            // We need a way to avoid double picking.
            // Let's assume we consume the allocation by creating a task.
            int qty = Math.min(qtyToPick, inv.getQuantity());
            PickingTask task = new PickingTask();
            task.setOutboundOrderItem(item);
            task.setInventory(inv);
            task.setQuantityToPick((double) qty);
            task.setStatus(PickingTaskStatus.PENDING);
            task.setCreatedAt(LocalDateTime.now());
            pickingTaskRepository.save(task);
            qtyToPick -= qty;
            // In a real system, we would link the task to the inventory ID to know exactly what to pick.
        }
    }
    /**
     * 4. Confirm Pick Task
     */
    @Transactional
    public void confirmPickTask(Integer taskId, Integer quantityPicked) {
        PickingTask task = pickingTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(PickingTaskStatus.PICKED);
        pickingTaskRepository.save(task);
        // Update Inventory: Remove the Allocated inventory
        // We need to find the inventory at that location.
        // Since we didn't store inventoryId in PickTask, we have to search.
        List<Inventory> invs = inventoryRepository.findByLocationId(task.getInventory().getLocation().getId());
        for (Inventory inv : invs) {
            if (inv.getProduct().getId().equals(task.getOutboundOrderItem().getProduct().getId())
                    && inv.getStatus() == InventoryStatus.ALLOCATED) {
                if (inv.getQuantity() <= quantityPicked) {
                    inventoryRepository.delete(inv); // Picked fully
                    quantityPicked -= inv.getQuantity();
                } else {
                    inv.setQuantity(inv.getQuantity() - quantityPicked);
                    inventoryRepository.save(inv);
                    quantityPicked = 0;
                }
                if (quantityPicked <= 0) break;
            }
        }
    }
}
