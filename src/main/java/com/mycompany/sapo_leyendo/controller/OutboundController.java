package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.OutboundOrderItem;
import com.mycompany.sapo_leyendo.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @GetMapping
    public List<OutboundOrder> getAllOutboundOrders() {
        return outboundService.getAllOutboundOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutboundOrder> getOutboundOrderById(@PathVariable Integer id) {
        return outboundService.getOutboundOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public OutboundOrder createOutboundOrder(@RequestBody OutboundOrder order) {
        // Set createdAt if not provided
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOutboundOrder(order));
        }
        return outboundService.saveOutboundOrder(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OutboundOrder> updateOutboundOrder(@PathVariable Integer id, @RequestBody OutboundOrder order) {
        return outboundService.getOutboundOrderById(id)
                .map(existing -> {
                    order.setId(id);
                    if (order.getItems() != null) {
                        order.getItems().forEach(item -> item.setOutboundOrder(order));
                    }
                    return ResponseEntity.ok(outboundService.saveOutboundOrder(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutboundOrder(@PathVariable Integer id) {
        return outboundService.getOutboundOrderById(id)
                .map(order -> {
                    outboundService.deleteOutboundOrder(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== OUTBOUND ORDER ITEMS =====

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OutboundOrderItem>> getOrderItems(@PathVariable Integer orderId) {
        return outboundService.getOutboundOrderById(orderId)
                .map(order -> ResponseEntity.ok(outboundService.getItemsByOrderId(orderId)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OutboundOrderItem> addItemToOrder(
            @PathVariable Integer orderId,
            @RequestBody OutboundOrderItem item) {
        return outboundService.addItemToOrder(orderId, item)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OutboundOrderItem> updateOrderItem(
            @PathVariable Integer orderId,
            @PathVariable Integer itemId,
            @RequestBody OutboundOrderItem item) {
        return outboundService.updateOrderItem(orderId, itemId, item)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromOrder(
            @PathVariable Integer orderId,
            @PathVariable Integer itemId) {
        if (outboundService.removeItemFromOrder(orderId, itemId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
