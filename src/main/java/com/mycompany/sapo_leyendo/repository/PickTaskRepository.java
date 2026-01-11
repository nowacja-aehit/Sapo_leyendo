package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.PickTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickTaskRepository extends JpaRepository<PickTask, Integer> {
}
