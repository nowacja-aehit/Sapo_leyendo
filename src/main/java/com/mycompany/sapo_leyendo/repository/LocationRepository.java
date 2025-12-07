package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Location> findFirstByLocationTypeNameAndIsActiveTrue(String typeName);
}
