package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.PickingTask;
import com.mycompany.sapo_leyendo.model.Wave;
import com.mycompany.sapo_leyendo.service.PickingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/picking")
@CrossOrigin(origins = "http://localhost:5173")
public class PickingController {

    @Autowired
    private PickingService pickingService;

    @PostMapping("/waves")
    public ResponseEntity<Wave> createWave(@RequestBody List<Integer> outboundOrderIds) {
        return ResponseEntity.ok(pickingService.createWave(outboundOrderIds));
    }

    @PostMapping("/waves/{waveId}/run")
    public ResponseEntity<Wave> runWave(@PathVariable UUID waveId, @RequestBody List<Integer> outboundOrderIds) {
        return ResponseEntity.ok(pickingService.runWave(waveId, outboundOrderIds));
    }

    @GetMapping("/waves/{waveId}/tasks")
    public ResponseEntity<List<PickingTask>> getPickingTasks(@PathVariable UUID waveId) {
        return ResponseEntity.ok(pickingService.getPickingTasks(waveId));
    }

    @PostMapping("/tasks/{taskId}/confirm")
    public ResponseEntity<Void> confirmTask(@PathVariable Integer taskId, @RequestParam Integer quantityPicked) {
        pickingService.confirmPickTask(taskId, quantityPicked);
        return ResponseEntity.ok().build();
    }
}
