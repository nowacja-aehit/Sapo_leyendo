package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.ReceiveItemRequest;
import com.mycompany.sapo_leyendo.model.InboundOrder;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.Receipt;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import com.mycompany.sapo_leyendo.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<InboundOrder> getAllInboundOrders() {
        return inboundService.getAllInboundOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InboundOrder> getInboundOrderById(@PathVariable Integer id) {
        return inboundService.getInboundOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createInboundOrder(@RequestBody InboundOrder order) {
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("PLANNED");
        }
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                item.setInboundOrder(order);
                // Resolve Product from productId
                if (item.getProduct() == null && item.getProductId() != null) {
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null);
                    if (product == null) {
                        return ResponseEntity.badRequest()
                                .body("Product not found with id: " + item.getProductId());
                    }
                    item.setProduct(product);
                }
                // Set default quantity field name mapping
                if (item.getQuantityExpected() == null) {
                    item.setQuantityExpected(0);
                }
            }
        }
        return ResponseEntity.ok(inboundService.saveInboundOrder(order));
    }

    @PostMapping("/receive")
    public ResponseEntity<?> receiveItem(@RequestBody ReceiveItemRequest request) {
        // Validate required fields
        if (request.getInboundOrderItemId() == null) {
            return ResponseEntity.badRequest().body("inboundOrderItemId is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("quantity must be a positive number");
        }
        
        try {
            Receipt receipt = inboundService.receiveItem(
                    request.getInboundOrderItemId(),
                    request.getLpn() != null ? request.getLpn() : inboundService.generateLpn(),
                    request.getQuantity(),
                    request.getOperatorId(),
                    request.getDamageCode()
            );
            return ResponseEntity.ok(receipt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/appointments")
    public ResponseEntity<com.mycompany.sapo_leyendo.model.DockAppointment> scheduleDock(
            @RequestParam Integer inboundOrderId,
            @RequestParam Integer dockId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String carrierName) {
        return ResponseEntity.ok(inboundService.scheduleDock(
                inboundOrderId,
                dockId,
                java.time.LocalDateTime.parse(startTime),
                java.time.LocalDateTime.parse(endTime),
                carrierName
        ));
    }

    @GetMapping("/lpn/generate")
    public ResponseEntity<String> generateLpn() {
        return ResponseEntity.ok(inboundService.generateLpn());
    }
}
