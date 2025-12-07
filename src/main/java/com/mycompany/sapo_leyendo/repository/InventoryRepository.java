package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    List<Inventory> findByProductId(Integer productId);
    List<Inventory> findByLocationId(Integer locationId);
}
