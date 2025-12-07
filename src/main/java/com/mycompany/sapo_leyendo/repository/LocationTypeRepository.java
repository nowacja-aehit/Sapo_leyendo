package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, Integer> {
}
