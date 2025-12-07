package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.TransportLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportLoadRepository extends JpaRepository<TransportLoad, Integer> {
}
