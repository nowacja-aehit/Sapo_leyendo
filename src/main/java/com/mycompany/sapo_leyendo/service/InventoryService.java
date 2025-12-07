package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoryByProduct(Integer productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> getInventoryByLocation(Integer locationId) {
        return inventoryRepository.findByLocationId(locationId);
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
}
