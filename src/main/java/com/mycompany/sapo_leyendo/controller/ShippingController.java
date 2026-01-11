package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Carrier;
import com.mycompany.sapo_leyendo.model.Manifest;
import com.mycompany.sapo_leyendo.model.Shipment;
import com.mycompany.sapo_leyendo.model.TransportLoad;
import com.mycompany.sapo_leyendo.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    // ===== CARRIERS =====
    
    @GetMapping("/carriers")
    public List<Carrier> getAllCarriers() {
        return shippingService.getAllCarriers();
    }

    @GetMapping("/carriers/{id}")
    public ResponseEntity<Carrier> getCarrierById(@PathVariable Integer id) {
        return shippingService.getCarrierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== SHIPMENTS =====
    
    @GetMapping("/shipments")
    public List<Shipment> getAllShipments() {
        return shippingService.getAllShipments();
    }

    @GetMapping("/shipments/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Integer id) {
        return shippingService.getShipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/shipments")
    public ResponseEntity<Shipment> createShipment(
            @RequestParam Integer outboundOrderId,
            @RequestParam Integer carrierId,
            @RequestParam(required = false) String trackingNumber) {
        return ResponseEntity.ok(shippingService.createShipment(outboundOrderId, carrierId, trackingNumber));
    }

    @PutMapping("/shipments/{id}")
    public ResponseEntity<Shipment> updateShipment(
            @PathVariable Integer id,
            @RequestBody Shipment shipment) {
        return shippingService.updateShipment(id, shipment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== LOADS =====

    @GetMapping("/loads")
    public List<TransportLoad> getAllLoads() {
        return shippingService.getAllLoads();
    }

    @PostMapping("/loads")
    public ResponseEntity<TransportLoad> createLoad(
            @RequestParam Integer carrierId,
            @RequestParam(required = false) String trailerNumber,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) String driverPhone) {
        return ResponseEntity.ok(shippingService.createLoad(carrierId, trailerNumber, driverName, driverPhone));
    }

    @PostMapping("/loads/{loadId}/assign/{shipmentId}")
    public ResponseEntity<Void> assignShipmentToLoad(@PathVariable Integer loadId, @PathVariable Integer shipmentId) {
        shippingService.assignShipmentToLoad(loadId, shipmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/loads/{loadId}/dispatch")
    public ResponseEntity<Manifest> dispatchLoad(@PathVariable Integer loadId) {
        return ResponseEntity.ok(shippingService.dispatchLoad(loadId));
    }
}
