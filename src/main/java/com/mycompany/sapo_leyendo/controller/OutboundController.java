package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound")
@CrossOrigin(origins = "http://localhost:5173")
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
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOutboundOrder(order));
        }
        return outboundService.saveOutboundOrder(order);
    }
}
