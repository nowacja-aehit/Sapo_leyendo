package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.PackingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingMaterialRepository extends JpaRepository<PackingMaterial, Integer> {
}
