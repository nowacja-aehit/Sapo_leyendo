package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.model.InventoryStatus;
import com.mycompany.sapo_leyendo.model.Location;
import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.UnitOfMeasure;
import com.mycompany.sapo_leyendo.service.InventoryService;
import com.mycompany.sapo_leyendo.service.LocationService;
import com.mycompany.sapo_leyendo.service.OutboundService;
import com.mycompany.sapo_leyendo.service.ProductService;
import com.mycompany.sapo_leyendo.repository.UnitOfMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private UnitOfMeasureRepository unitOfMeasureRepository;
    
    @Autowired
    private OutboundService outboundService;

    @GetMapping("/inventory")
    public List<Inventory> getInventory() {
        return inventoryService.getAllInventory();
    }

    @PostMapping("/inventory")
    public ResponseEntity<Inventory> createInventory(@RequestBody Map<String, Object> request) {
        Inventory inventory = new Inventory();
        
        // Pobierz Product z bazy
        Integer productId = (Integer) request.get("productId");
        if (productId != null) {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            inventory.setProduct(product);
        }
        
        // Pobierz Location z bazy
        Integer locationId = (Integer) request.get("locationId");
        if (locationId != null) {
            Location location = locationService.getLocationById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));
            inventory.setLocation(location);
        }
        
        // Ustaw quantity
        if (request.containsKey("quantity")) {
            inventory.setQuantity((Integer) request.get("quantity"));
        }
        
        // Ustaw domyślny UOM jeśli nie podano
        if (request.containsKey("uomId")) {
            Integer uomId = (Integer) request.get("uomId");
            UnitOfMeasure uom = unitOfMeasureRepository.findById(uomId)
                    .orElseThrow(() -> new RuntimeException("UOM not found: " + uomId));
            inventory.setUom(uom);
        } else {
            // Użyj domyślnego UOM z produktu lub utwórz nowy
            Integer baseUomId = inventory.getProduct().getIdBaseUom();
            UnitOfMeasure uom;
            
            if (baseUomId != null) {
                uom = unitOfMeasureRepository.findById(baseUomId).orElse(null);
            } else {
                uom = null;
            }
            
            // Jeśli nie znaleziono, spróbuj ID=1
            if (uom == null) {
                uom = unitOfMeasureRepository.findById(1).orElse(null);
            }
            
            // Jeśli nadal null, utwórz domyślny UOM
            if (uom == null) {
                uom = new UnitOfMeasure();
                uom.setId(1);
                uom.setCode("EA");
                uom.setName("Each");
                uom = unitOfMeasureRepository.save(uom);
            }
            
            inventory.setUom(uom);
        }
        
        // Ustaw pozostałe pola
        if (request.containsKey("status")) {
            String statusStr = (String) request.get("status");
            try {
                InventoryStatus status = InventoryStatus.valueOf(statusStr.toUpperCase());
                inventory.setStatus(status);
            } catch (IllegalArgumentException e) {
                inventory.setStatus(InventoryStatus.AVAILABLE);
            }
        }
        if (request.containsKey("batchNumber")) {
            inventory.setBatchNumber((String) request.get("batchNumber"));
        }
        
        // Ustaw receivedAt (NOT NULL w bazie danych)
        inventory.setReceivedAt(java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(inventoryService.saveInventory(inventory));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        return inventoryService.getInventoryById(id)
                .map(existing -> {
                    // Aktualizuj tylko przesłane pola
                    if (updates.containsKey("quantity")) {
                        existing.setQuantity((Integer) updates.get("quantity"));
                    }
                    if (updates.containsKey("productId")) {
                        Integer productId = (Integer) updates.get("productId");
                        Product product = productService.getProductById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                        existing.setProduct(product);
                    }
                    if (updates.containsKey("locationId")) {
                        Integer locationId = (Integer) updates.get("locationId");
                        Location location = locationService.getLocationById(locationId)
                                .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));
                        existing.setLocation(location);
                    }
                    // Support location by code/name (e.g., "A-01-01")
                    if (updates.containsKey("locationCode")) {
                        String locationCode = (String) updates.get("locationCode");
                        Location location = locationService.getLocationByName(locationCode)
                                .orElseThrow(() -> new RuntimeException("Location not found by code: " + locationCode));
                        existing.setLocation(location);
                    }
                    if (updates.containsKey("status")) {
                        String statusStr = (String) updates.get("status");
                        try {
                            InventoryStatus status = InventoryStatus.valueOf(statusStr.toUpperCase());
                            existing.setStatus(status);
                        } catch (IllegalArgumentException e) {
                            existing.setStatus(InventoryStatus.AVAILABLE);
                        }
                    }
                    if (updates.containsKey("batchNumber")) {
                        existing.setBatchNumber((String) updates.get("batchNumber"));
                    }
                    // Handle price update - set inventory-specific unit price
                    if (updates.containsKey("price")) {
                        Object priceObj = updates.get("price");
                        if (priceObj != null) {
                            java.math.BigDecimal price;
                            if (priceObj instanceof Number) {
                                price = java.math.BigDecimal.valueOf(((Number) priceObj).doubleValue());
                            } else {
                                price = new java.math.BigDecimal(priceObj.toString());
                            }
                            existing.setUnitPrice(price);
                        }
                    }
                    
                    return ResponseEntity.ok(inventoryService.saveInventory(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Integer id) {
        inventoryService.getInventoryById(id).ifPresent(inv -> {
            inventoryService.deleteInventory(id);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", 0);
        stats.put("totalInventoryValue", 0.0);
        stats.put("lowStockItems", 0);
        stats.put("outOfStockItems", 0);
        stats.put("totalOrders", 0);
        stats.put("pendingOrders", 0);
        return stats;
    }

    @GetMapping("/orders")
    public List<OutboundOrder> getDashboardOrders() {
        return outboundService.getAllOutboundOrders();
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createDashboardOrder(@RequestBody Map<String, Object> order) {
        // Forward to OutboundController logic or just accept for dashboard display
        Map<String, Object> response = new HashMap<>(order);
        if (!response.containsKey("id")) {
            response.put("id", java.util.UUID.randomUUID().toString());
        }
        // Ustaw domyślny status jeśli nie podano
        if (!response.containsKey("status")) {
            response.put("status", "NEW");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shipments")
    public List<Map<String, Object>> getDashboardShipments() {
        return List.of();
    }
}
