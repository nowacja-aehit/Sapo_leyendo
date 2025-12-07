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

    /**
     * Suggests the best packing material based on total volume of products.
     * Simplified Cartonization Logic.
     */
    public PackingMaterial suggestPackingMaterial(List<Product> products) {
        double totalVolume = 0.0;
        for (Product p : products) {
            // Assuming Product has dimensions, if not we use a default or skip
            // Let's assume we have a getVolume() method or calculate it
            // For now, we mock it as 1.0 per unit if dimensions missing
            totalVolume += 1.0; 
        }

        // Find smallest box that fits the volume
        // In real world, we need 3D bin packing algorithm.
        // Here we just compare volume.
        List<PackingMaterial> materials = packingMaterialRepository.findAll();
        
        double finalVolume = totalVolume;
        return materials.stream()
                .filter(m -> calculateVolume(m) >= finalVolume)
                .sorted((m1, m2) -> Double.compare(calculateVolume(m1), calculateVolume(m2)))
                .findFirst()
                .orElse(null); // No box fits
    }

    private double calculateVolume(PackingMaterial m) {
        // Assuming dimensions are stored in a way we can calculate volume
        // For this mock, we return a dummy value based on ID or name
        return 100.0; 
    }
}
