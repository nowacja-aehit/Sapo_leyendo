package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.PackingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingStationRepository extends JpaRepository<PackingStation, Integer> {
}
