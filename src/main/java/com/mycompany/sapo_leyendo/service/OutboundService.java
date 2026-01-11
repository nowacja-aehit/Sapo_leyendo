package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.OutboundOrderItem;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.UnitOfMeasure;
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;
import com.mycompany.sapo_leyendo.repository.OutboundOrderItemRepository;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import com.mycompany.sapo_leyendo.repository.UnitOfMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OutboundService {

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private OutboundOrderItemRepository outboundOrderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UnitOfMeasureRepository uomRepository;

    public List<OutboundOrder> getAllOutboundOrders() {
        return outboundOrderRepository.findAll();
    }

    public Optional<OutboundOrder> getOutboundOrderById(Integer id) {
        return outboundOrderRepository.findById(id);
    }

    public OutboundOrder saveOutboundOrder(OutboundOrder order) {
        // Auto-generate unique reference number if not provided or already exists
        if (order.getReferenceNumber() == null || order.getReferenceNumber().isEmpty() ||
            outboundOrderRepository.existsByReferenceNumber(order.getReferenceNumber())) {
            order.setReferenceNumber("OUT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
        }
        // Set createdAt if not provided
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(java.time.LocalDateTime.now());
        }
        return outboundOrderRepository.save(order);
    }

    public void deleteOutboundOrder(Integer id) {
        outboundOrderRepository.deleteById(id);
    }

    // ===== ITEM OPERATIONS =====

    public List<OutboundOrderItem> getItemsByOrderId(Integer orderId) {
        return outboundOrderItemRepository.findByOutboundOrderId(orderId);
    }

    @Transactional
    public Optional<OutboundOrderItem> addItemToOrder(Integer orderId, OutboundOrderItem item) {
        return outboundOrderRepository.findById(orderId)
                .map(order -> {
                    item.setOutboundOrder(order);
                    
                    // Enrich item with product data if productId is provided
                    if (item.getProduct() != null && item.getProduct().getId() != null) {
                        Product product = productRepository.findById(item.getProduct().getId())
                                .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));
                        item.setProduct(product);
                        item.setProductName(product.getName());
                        item.setSku(product.getSku());
                        
                        // Calculate line total if unit price is set
                        if (item.getUnitPrice() != null && item.getQuantityOrdered() != null) {
                            item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered())));
                        }
                    }
                    
                    // Set default UOM if not provided (use product's base UOM or fallback to ID 1 = SZT)
                    if (item.getUom() == null) {
                        if (item.getProduct() != null && item.getProduct().getIdBaseUom() != null) {
                            UnitOfMeasure uom = uomRepository.findById(item.getProduct().getIdBaseUom())
                                    .orElseGet(() -> uomRepository.findById(1).orElse(null));
                            item.setUom(uom);
                        } else {
                            // Default to SZT (id=1)
                            item.setUom(uomRepository.findById(1).orElse(null));
                        }
                    }
                    
                    if (item.getQuantityPicked() == null) item.setQuantityPicked(0.0);
                    if (item.getQuantityShipped() == null) item.setQuantityShipped(0.0);
                    if (item.getStatus() == null) item.setStatus("PENDING");
                    
                    return outboundOrderItemRepository.save(item);
                });
    }

    @Transactional
    public Optional<OutboundOrderItem> updateOrderItem(Integer orderId, Integer itemId, OutboundOrderItem item) {
        return outboundOrderItemRepository.findById(itemId)
                .filter(existing -> existing.getOutboundOrder().getId().equals(orderId))
                .map(existing -> {
                    item.setId(itemId);
                    item.setOutboundOrder(existing.getOutboundOrder());
                    
                    // Re-calculate line total if values changed
                    if (item.getUnitPrice() != null && item.getQuantityOrdered() != null) {
                        item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered())));
                    }
                    
                    return outboundOrderItemRepository.save(item);
                });
    }

    @Transactional
    public boolean removeItemFromOrder(Integer orderId, Integer itemId) {
        return outboundOrderItemRepository.findById(itemId)
                .filter(item -> item.getOutboundOrder().getId().equals(orderId))
                .map(item -> {
                    outboundOrderItemRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }
}
