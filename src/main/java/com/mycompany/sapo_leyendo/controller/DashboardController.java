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
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

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

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;


    @GetMapping("/shipments")
    public ResponseEntity<List<Map<String, Object>>> getShipments() {
        try {
            List<Shipment> shipments = shipmentRepository.findAll();
            List<Map<String, Object>> response = shipments.stream().map(shipment -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", shipment.getId());
                map.put("trackingNumber", shipment.getTrackingNumber());
                map.put("destination", shipment.getOutboundOrder() != null ? shipment.getOutboundOrder().getDestination() : "Unknown");
                map.put("carrierId", shipment.getCarrierId());
                map.put("status", shipment.getStatus().toString());
                map.put("parcelCount", shipment.getParcels().size());
                map.put("shippedAt", shipment.getShippedAt() != null ? shipment.getShippedAt().toString() : "N/A");
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving shipments", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<Map<String, Object>>> getInventory() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        List<Map<String, Object>> response = inventoryList.stream().map(inventory -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inventory.getId());

            Product product = inventory.getProduct();
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productSku", product.getSku());
            }

            Location location = inventory.getLocation();
            if (location != null) {
                map.put("locationName", location.getName());
            }

            map.put("quantity", inventory.getQuantity());
            map.put("status", inventory.getStatus().toString());
            map.put("batchNumber", inventory.getBatchNumber());
            map.put("receivedAt", inventory.getReceivedAt() != null ? inventory.getReceivedAt().toString() : "N/A");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrders() {
        List<com.mycompany.sapo_leyendo.model.OutboundOrder> orders = outboundOrderRepository.findAll();
        List<Map<String, Object>> response = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("referenceNumber", order.getReferenceNumber());
            map.put("destination", order.getDestination());
            map.put("status", order.getStatus());
            map.put("itemCount", order.getItems().size());
            map.put("destination", order.getDestination());
            map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
