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
    public TransportLoad createLoad(Integer carrierId, String vehiclePlateNumber, String driverName, String driverPhone, Integer dockId) {
        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new RuntimeException("Carrier not found"));

        TransportLoad load = new TransportLoad();
        load.setCarrier(carrier);
        load.setVehiclePlateNumber(vehiclePlateNumber);
        load.setDriverName(driverName);
        load.setDriverPhone(driverPhone);
        load.setDockId(dockId);
        load.setStatus(LoadStatus.PLANNED);
        
        return transportLoadRepository.save(load);
    }

    @Transactional
    public void assignShipmentToLoad(Integer loadId, Integer shipmentId) {
        TransportLoad load = transportLoadRepository.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (load.getStatus() == LoadStatus.DISPATCHED || load.getStatus() == LoadStatus.DELIVERED) {
            throw new RuntimeException("Cannot assign shipment to a dispatched or delivered load");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        shipment.setTransportLoad(load);
        shipmentRepository.save(shipment);

        if (load.getStatus() == LoadStatus.PLANNED) {
            load.setStatus(LoadStatus.LOADING);
            transportLoadRepository.save(load);
        }
    }

    @Transactional
    public Manifest dispatchLoad(Integer loadId) {
        TransportLoad load = transportLoadRepository.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (load.getStatus() == LoadStatus.DISPATCHED) {
            throw new RuntimeException("Load is already dispatched");
        }

        List<Shipment> shipments = load.getShipments();
        if (shipments == null || shipments.isEmpty()) {
            throw new RuntimeException("Cannot dispatch an empty load");
        }

        double totalWeight = 0.0;
        int totalParcels = 0;

        for (Shipment shipment : shipments) {
            shipment.setStatus(ShipmentStatus.SHIPPED);
            shipment.setShippedAt(LocalDateTime.now()); 
            
            totalWeight += (shipment.getTotalWeightKg() != null ? shipment.getTotalWeightKg() : 0.0);
            totalParcels += (shipment.getParcels() != null ? shipment.getParcels().size() : 0);
            
            // Update Order Status
            OutboundOrder order = shipment.getOutboundOrder();
            if (order != null) {
                order.setStatus("SHIPPED");
                outboundOrderRepository.save(order);
            }
            
            shipmentRepository.save(shipment);
        }

        load.setStatus(LoadStatus.DISPATCHED);
        load.setDepartureTime(LocalDateTime.now());
        transportLoadRepository.save(load);

        Manifest manifest = new Manifest();
        manifest.setTransportLoad(load);
        manifest.setTotalParcels(totalParcels);
        manifest.setTotalWeight(totalWeight);
        manifest.setGenerationDate(LocalDateTime.now());
        manifest.setCarrierManifestId(UUID.randomUUID().toString()); // Mock generation

        return manifestRepository.save(manifest);
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
