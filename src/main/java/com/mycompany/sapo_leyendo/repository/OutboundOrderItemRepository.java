package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.OutboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItem, Integer> {
    List<OutboundOrderItem> findByOutboundOrderId(Integer outboundOrderId);
}
