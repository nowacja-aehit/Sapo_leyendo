package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.QcInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QcInspectionRepository extends JpaRepository<QcInspection, Integer> {
}
