package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.InboundOrder;
import com.mycompany.sapo_leyendo.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound")
@CrossOrigin(origins = "http://localhost:5173")
public class InboundController {

    @Autowired
    private InboundService inboundService;

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
    public InboundOrder createInboundOrder(@RequestBody InboundOrder order) {
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setInboundOrder(order));
        }
        return inboundService.saveInboundOrder(order);
    }
}
