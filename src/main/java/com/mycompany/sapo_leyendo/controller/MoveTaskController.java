package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.MoveTask;
import com.mycompany.sapo_leyendo.service.MoveTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/move-tasks")
public class MoveTaskController {

    @Autowired
    private MoveTaskService moveTaskService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<MoveTask> completeTask(@PathVariable Integer id) {
        MoveTask task = moveTaskService.completeTask(id);
        return ResponseEntity.ok(task);
    }
}
