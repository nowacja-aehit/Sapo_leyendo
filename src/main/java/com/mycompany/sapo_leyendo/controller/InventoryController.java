package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.model.MoveTask;
import com.mycompany.sapo_leyendo.dto.MoveTaskRequest;
import com.mycompany.sapo_leyendo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/product/{productId}")
    public List<Inventory> getInventoryByProduct(@PathVariable Integer productId) {
        return inventoryService.getInventoryByProduct(productId);
    }

    @GetMapping("/location/{locationId}")
    public List<Inventory> getInventoryByLocation(@PathVariable Integer locationId) {
        return inventoryService.getInventoryByLocation(locationId);
    }

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.saveInventory(inventory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Integer id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/putaway/{receiptId}")
    public ResponseEntity<MoveTask> createPutAwayTask(@PathVariable Integer receiptId) {
        try {
            MoveTask task = inventoryService.createPutAwayTask(receiptId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tasks/pending")
    public List<MoveTask> getPendingTasks() {
        return inventoryService.getPendingTasks();
    }

    @PostMapping("/tasks")
    public ResponseEntity<MoveTask> createMoveTask(@RequestBody MoveTaskRequest request) {
        MoveTask task = inventoryService.createManualMoveTask(request.inventoryId(), request.targetLocationId());
        return ResponseEntity.ok(task);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Integer taskId) {
        inventoryService.completeTask(taskId);
        return ResponseEntity.ok().build();
    }
}
