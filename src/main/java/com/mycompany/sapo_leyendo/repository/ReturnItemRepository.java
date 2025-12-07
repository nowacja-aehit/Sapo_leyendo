package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Integer> {
}
