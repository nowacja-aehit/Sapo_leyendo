package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.InboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboundOrderItemRepository extends JpaRepository<InboundOrderItem, Integer> {
}
