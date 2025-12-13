package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.dashboard.DashboardInventoryItem;
import com.mycompany.sapo_leyendo.dto.dashboard.DashboardOrder;
import com.mycompany.sapo_leyendo.dto.dashboard.DashboardShipment;
import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.model.InventoryStatus;
import com.mycompany.sapo_leyendo.model.Location;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.Shipment;
import com.mycompany.sapo_leyendo.service.InventoryService;
import com.mycompany.sapo_leyendo.service.OutboundService;
import com.mycompany.sapo_leyendo.repository.ShipmentRepository;
import com.mycompany.sapo_leyendo.repository.CarrierRepository;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import com.mycompany.sapo_leyendo.repository.LocationRepository;
import com.mycompany.sapo_leyendo.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class DashboardController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/inventory")
    public List<DashboardInventoryItem> getInventory() {
        return inventoryService.getAllInventory().stream()
            .map(inv -> new DashboardInventoryItem(
                String.valueOf(inv.getId()),
                inv.getProduct().getName(),
                inv.getProduct().getSku(),
                "Category " + inv.getProduct().getIdCategory(), // Simplified
                inv.getQuantity(),
                10, // Hardcoded reorder level
                inv.getLocation().getName(),
                inv.getStatus().name(),
                100.0, // Hardcoded price
                java.time.LocalDate.now().toString() // Hardcoded date
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/inventory")
    public ResponseEntity<DashboardInventoryItem> createInventoryItem(@RequestBody DashboardInventoryItem item) {
        // 1. Find or Create Product
        Product product = productRepository.findBySku(item.sku())
                .orElseGet(() -> {
                    Product newProduct = new Product();
                    newProduct.setSku(item.sku());
                    newProduct.setName(item.name());
                    newProduct.setIdBaseUom(1); // Default UOM
                    return productRepository.save(newProduct);
                });

        // 2. Find Location (or default)
        Location location = locationRepository.findByName(item.location())
                .orElseGet(() -> locationRepository.findAll().stream().findFirst().orElseThrow());

        // 3. Create Inventory
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setLocation(location);
        inventory.setQuantity(item.quantity());
        inventory.setStatus(InventoryStatus.AVAILABLE);
        
        inventory = inventoryRepository.save(inventory);

        return ResponseEntity.ok(new DashboardInventoryItem(
                String.valueOf(inventory.getId()),
                product.getName(),
                product.getSku(),
                "Category " + product.getIdCategory(),
                inventory.getQuantity(),
                10,
                location.getName(),
                inventory.getStatus().name(),
                100.0,
                java.time.LocalDate.now().toString()
        ));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<DashboardInventoryItem> updateInventoryItem(@PathVariable Integer id, @RequestBody DashboardInventoryItem item) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow();
        
        // Update Product info
        Product product = inventory.getProduct();
        if (!product.getName().equals(item.name()) || !product.getSku().equals(item.sku())) {
            product.setName(item.name());
            product.setSku(item.sku());
            productRepository.save(product);
        }

        // Update Inventory info
        inventory.setQuantity(item.quantity());
        
        // Update Location if changed
        if (!inventory.getLocation().getName().equals(item.location())) {
             Location location = locationRepository.findByName(item.location())
                .orElseGet(() -> locationRepository.findAll().stream().findFirst().orElseThrow());
             inventory.setLocation(location);
        }

        inventoryRepository.save(inventory);

        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Integer id) {
        inventoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public List<DashboardOrder> getOrders() {
        return outboundService.getAllOutboundOrders().stream()
            .map(order -> new DashboardOrder(
                String.valueOf(order.getId()),
                order.getReferenceNumber(),
                order.getDestination(),
                order.getItems() != null ? order.getItems().size() : 0,
                0.0, // Hardcoded total
                order.getStatus(),
                order.getShipDate() != null ? order.getShipDate().toString() : "",
                "Medium" // Hardcoded priority
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/shipments")
    public List<DashboardShipment> getShipments() {
        return shipmentRepository.findAll().stream()
            .map(shipment -> new DashboardShipment(
                String.valueOf(shipment.getId()),
                shipment.getTrackingNumber(),
                shipment.getOutboundOrder() != null ? shipment.getOutboundOrder().getDestination() : "Unknown",
                shipment.getCarrierId() != null ? "Carrier " + shipment.getCarrierId() : "Unknown",
                shipment.getStatus() != null ? shipment.getStatus().name() : "Unknown",
                shipment.getShippedAt() != null ? shipment.getShippedAt().toString() : "",
                shipment.getParcels() != null ? shipment.getParcels().size() : 0
            ))
            .collect(Collectors.toList());
    }
}
