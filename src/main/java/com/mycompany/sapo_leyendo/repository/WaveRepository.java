package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Wave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaveRepository extends JpaRepository<Wave, Integer> {
}
