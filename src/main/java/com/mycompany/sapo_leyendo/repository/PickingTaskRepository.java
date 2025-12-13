package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.PickingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickingTaskRepository extends JpaRepository<PickingTask, Integer> {
}
