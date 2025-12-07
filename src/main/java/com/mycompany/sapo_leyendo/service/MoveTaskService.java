package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.Inventory;
import com.mycompany.sapo_leyendo.model.MoveTask;
import com.mycompany.sapo_leyendo.model.MoveTaskStatus;
import com.mycompany.sapo_leyendo.repository.InventoryRepository;
import com.mycompany.sapo_leyendo.repository.MoveTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MoveTaskService {

    @Autowired
    private MoveTaskRepository moveTaskRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public MoveTask completeTask(Integer taskId) {
        MoveTask task = moveTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getStatus() == MoveTaskStatus.COMPLETED) {
            return task;
        }

        // Update Task Status
        task.setStatus(MoveTaskStatus.COMPLETED);
        moveTaskRepository.save(task);

        // Move Inventory
        Inventory inventory = task.getInventory();
        inventory.setLocation(task.getTargetLocation());
        inventoryRepository.save(inventory);

        return task;
    }
}
