package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Parcel;
import com.mycompany.sapo_leyendo.model.Shipment;
import com.mycompany.sapo_leyendo.service.PackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/packing")
@CrossOrigin(origins = "http://localhost:5173")
public class PackingController {

    @Autowired
    private PackingService packingService;

    @PostMapping("/shipments/start/{outboundOrderId}")
    public ResponseEntity<Shipment> startPacking(@PathVariable Integer outboundOrderId) {
        return ResponseEntity.ok(packingService.startPacking(outboundOrderId));
    }

    @PostMapping("/shipments/{shipmentId}/parcels")
    public ResponseEntity<Parcel> createParcel(@PathVariable Integer shipmentId, @RequestParam Integer packingMaterialId) {
        return ResponseEntity.ok(packingService.createParcel(shipmentId, packingMaterialId));
    }

    @PostMapping("/parcels/{parcelId}/items")
    public ResponseEntity<Parcel> addItemToParcel(@PathVariable Integer parcelId, @RequestParam Integer productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(packingService.addItemToParcel(parcelId, productId, quantity));
    }
    
    @PostMapping("/shipments/{shipmentId}/close")
    public ResponseEntity<Shipment> closeShipment(@PathVariable Integer shipmentId) {
        return ResponseEntity.ok(packingService.closeShipment(shipmentId));
    }
}
