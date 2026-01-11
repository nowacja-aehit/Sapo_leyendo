package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.PickingTask;
import com.mycompany.sapo_leyendo.model.Wave;
import com.mycompany.sapo_leyendo.repository.PickingTaskRepository;
import com.mycompany.sapo_leyendo.service.PickingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picking")
public class PickingController {

    @Autowired
    private PickingService pickingService;

    @Autowired
    private PickingTaskRepository pickingTaskRepository;

    /**
     * Get all picking tasks - for dashboard/list view
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<PickingTask>> getAllPickingTasks() {
        return ResponseEntity.ok(pickingTaskRepository.findAll());
    }

    @PostMapping("/waves")
    public ResponseEntity<Wave> createWave(@RequestBody List<Integer> outboundOrderIds) {
        return ResponseEntity.ok(pickingService.createWave(outboundOrderIds));
    }

    @PostMapping("/waves/{waveId}/run")
    public ResponseEntity<Wave> runWave(@PathVariable Integer waveId, @RequestBody List<Integer> outboundOrderIds) {
        return ResponseEntity.ok(pickingService.runWave(waveId, outboundOrderIds));
    }

    @GetMapping("/waves/{waveId}/tasks")
    public ResponseEntity<List<PickingTask>> getPickingTasks(@PathVariable Integer waveId) {
        return ResponseEntity.ok(pickingService.getPickingTasks(waveId));
    }

    @PostMapping("/tasks/{taskId}/confirm")
    public ResponseEntity<Void> confirmTask(@PathVariable Integer taskId, @RequestParam Integer quantityPicked) {
        pickingService.confirmPickTask(taskId, quantityPicked);
        return ResponseEntity.ok().build();
    }
}
