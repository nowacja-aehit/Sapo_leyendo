package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
