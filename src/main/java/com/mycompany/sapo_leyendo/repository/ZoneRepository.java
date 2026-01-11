package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    Optional<Zone> findByName(String name);
}
