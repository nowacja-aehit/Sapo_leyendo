package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShippingService {

    @Autowired
    private TransportLoadRepository transportLoadRepository;

    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ManifestRepository manifestRepository;
    
    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Transactional
    public TransportLoad createLoad(Integer carrierId, String trailerNumber, String driverName, String driverPhone) {
        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new RuntimeException("Carrier not found"));

        TransportLoad load = new TransportLoad();
        load.setLoadNumber("LOAD-" + System.currentTimeMillis());
        load.setCarrier(carrier);
        load.setTrailerNumber(trailerNumber);
        load.setDriverName(driverName);
        load.setDriverPhone(driverPhone);
        load.setStatus(LoadStatus.PLANNING);
        load.setCreatedAt(LocalDateTime.now());
        
        return transportLoadRepository.save(load);
    }

    @Transactional
    public void assignShipmentToLoad(Integer loadId, Integer shipmentId) {
        TransportLoad load = transportLoadRepository.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (load.getStatus() == LoadStatus.IN_TRANSIT || load.getStatus() == LoadStatus.DELIVERED) {
            throw new RuntimeException("Cannot assign shipment to a dispatched or delivered load");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        shipment.setTransportLoad(load);
        shipmentRepository.save(shipment);

        if (load.getStatus() == LoadStatus.PLANNING) {
            load.setStatus(LoadStatus.LOADING);
            transportLoadRepository.save(load);
        }
    }

    @Transactional
    public Manifest dispatchLoad(Integer loadId) {
        TransportLoad load = transportLoadRepository.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (load.getStatus() == LoadStatus.IN_TRANSIT) {
            throw new RuntimeException("Load is already dispatched");
        }

        List<Shipment> shipments = load.getShipments();
        if (shipments == null || shipments.isEmpty()) {
            throw new RuntimeException("Cannot dispatch an empty load");
        }

        for (Shipment shipment : shipments) {
            shipment.setStatus(ShipmentStatus.SHIPPED);
            shipment.setShippedAt(LocalDateTime.now()); 
            
            // Update Order Status
            OutboundOrder order = shipment.getOutboundOrder();
            if (order != null) {
                order.setStatus("SHIPPED");
                outboundOrderRepository.save(order);
            }
            
            shipmentRepository.save(shipment);
        }

        load.setStatus(LoadStatus.IN_TRANSIT);
        load.setActualDeparture(LocalDateTime.now());
        transportLoadRepository.save(load);

        Manifest manifest = new Manifest();
        manifest.setManifestNumber("MAN-" + System.currentTimeMillis());
        manifest.setTransportLoad(load);
        manifest.setCreatedAt(LocalDateTime.now());

        return manifestRepository.save(manifest);
    }

    // ===== CARRIER OPERATIONS =====
    
    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAll();
    }

    public java.util.Optional<Carrier> getCarrierById(Integer id) {
        return carrierRepository.findById(id);
    }

    // ===== SHIPMENT OPERATIONS =====
    
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public java.util.Optional<Shipment> getShipmentById(Integer id) {
        return shipmentRepository.findById(id);
    }

    @Transactional
    public Shipment createShipment(Integer outboundOrderId, Integer carrierId, String trackingNumber) {
        OutboundOrder order = outboundOrderRepository.findById(outboundOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + outboundOrderId));

        Shipment shipment = new Shipment();
        shipment.setOutboundOrder(order);
        shipment.setCarrierId(carrierId);
        shipment.setTrackingNumber(trackingNumber != null ? trackingNumber : generateTrackingNumber());
        shipment.setStatus(ShipmentStatus.PACKING);
        
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public java.util.Optional<Shipment> updateShipment(Integer id, Shipment updated) {
        return shipmentRepository.findById(id)
                .map(existing -> {
                    if (updated.getCarrierId() != null) existing.setCarrierId(updated.getCarrierId());
                    if (updated.getTrackingNumber() != null) existing.setTrackingNumber(updated.getTrackingNumber());
                    if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
                    if (updated.getTotalWeightKg() != null) existing.setTotalWeightKg(updated.getTotalWeightKg());
                    return shipmentRepository.save(existing);
                });
    }

    public List<TransportLoad> getAllLoads() {
        return transportLoadRepository.findAll();
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Rate Shopping Logic (Mock)
     * Finds the cheapest carrier for a given shipment.
     */
    public Carrier findBestCarrier(Double weight, String destinationZip) {
        List<Carrier> carriers = carrierRepository.findAll();
        
        // Mock logic: Randomly select one or pick based on simple rule
        // Real logic would call Carrier APIs
        return carriers.stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("No carriers available"));
    }
}
