package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.MoveTask;
import com.mycompany.sapo_leyendo.model.MoveTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveTaskRepository extends JpaRepository<MoveTask, Integer> {
    List<MoveTask> findByStatus(MoveTaskStatus status);
}
