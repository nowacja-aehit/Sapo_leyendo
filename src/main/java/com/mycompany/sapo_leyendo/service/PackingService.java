package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PackingService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private ParcelItemRepository parcelItemRepository;

    @Autowired
    private PackingMaterialRepository packingMaterialRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Shipment startPacking(Integer outboundOrderId) {
        OutboundOrder order = outboundOrderRepository.findById(outboundOrderId)
                .orElseThrow(() -> new RuntimeException("Outbound Order not found"));

        Optional<Shipment> existingShipment = shipmentRepository.findByOutboundOrderId(outboundOrderId);
        if (existingShipment.isPresent()) {
            return existingShipment.get();
        }

        Shipment shipment = new Shipment();
        shipment.setOutboundOrder(order);
        shipment.setStatus(ShipmentStatus.PACKING);
        shipment.setTotalWeightKg(0.0);
        
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Parcel createParcel(Integer shipmentId, Integer packingMaterialId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        PackingMaterial material = packingMaterialRepository.findById(packingMaterialId)
                .orElseThrow(() -> new RuntimeException("Packing Material not found"));

        Parcel parcel = new Parcel();
        parcel.setShipment(shipment);
        parcel.setPackingMaterial(material);
        parcel.setWeightKg(material.getTareWeightKg()); // Initial weight is tare weight
        
        return parcelRepository.save(parcel);
    }

    @Transactional
    public Parcel addItemToParcel(Integer parcelId, Integer productId, Integer quantity) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ParcelItem item = new ParcelItem();
        item.setParcel(parcel);
        item.setProduct(product);
        item.setQuantity(quantity);
        
        parcelItemRepository.save(item);
        
        // Update parcel weight (simplified logic)
        if (product.getWeightKg() != null) {
            parcel.setWeightKg(parcel.getWeightKg() + (product.getWeightKg() * quantity));
            parcelRepository.save(parcel);
        }

        return parcel;
    }
    
    @Transactional
    public Shipment closeShipment(Integer shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        
        shipment.setStatus(ShipmentStatus.PACKED);
        
        // Calculate total weight
        double totalWeight = shipment.getParcels().stream()
                .mapToDouble(Parcel::getWeightKg)
                .sum();
        shipment.setTotalWeightKg(totalWeight);
        
        // Update Outbound Order status
        OutboundOrder order = shipment.getOutboundOrder();
        order.setStatus("PACKED");
        outboundOrderRepository.save(order);
        
        return shipmentRepository.save(shipment);
    }
}
