package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.GradingStatus;
import com.mycompany.sapo_leyendo.model.ReturnItem;
import com.mycompany.sapo_leyendo.model.RmaRequest;
import com.mycompany.sapo_leyendo.service.ReturnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnsController {

    @Autowired
    private ReturnsService returnsService;

    @GetMapping
    public List<RmaRequest> getAllRmaRequests() {
        return returnsService.getAllRmaRequests();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RmaRequest> getRmaRequestById(@PathVariable Integer id) {
        return returnsService.getRmaRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rma")
    public ResponseEntity<RmaRequest> createRmaRequest(@RequestParam Integer outboundOrderId, @RequestParam String reason) {
        return ResponseEntity.ok(returnsService.createRmaRequest(outboundOrderId, reason));
    }

    @PostMapping("/receive/{rmaId}")
    public ResponseEntity<RmaRequest> receiveReturn(@PathVariable Integer rmaId, @RequestParam String trackingNumber) {
        return ResponseEntity.ok(returnsService.receiveReturn(rmaId, trackingNumber));
    }

    @PostMapping("/grade/{rmaId}")
    public ResponseEntity<ReturnItem> gradeItem(
            @PathVariable Integer rmaId,
            @RequestParam Integer productId,
            @RequestParam GradingStatus grade,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(returnsService.gradeItem(rmaId, productId, grade, comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRmaRequest(@PathVariable Integer id) {
        return returnsService.getRmaRequestById(id)
                .map(rma -> {
                    returnsService.deleteRmaRequest(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
