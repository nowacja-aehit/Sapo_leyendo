package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Manifest;
import com.mycompany.sapo_leyendo.model.TransportLoad;
import com.mycompany.sapo_leyendo.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "http://localhost:5173")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @PostMapping("/loads")
    public ResponseEntity<TransportLoad> createLoad(
            @RequestParam Integer carrierId,
            @RequestParam String vehiclePlateNumber,
            @RequestParam String driverName,
            @RequestParam String driverPhone,
            @RequestParam Integer dockId) {
        return ResponseEntity.ok(shippingService.createLoad(carrierId, vehiclePlateNumber, driverName, driverPhone, dockId));
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
