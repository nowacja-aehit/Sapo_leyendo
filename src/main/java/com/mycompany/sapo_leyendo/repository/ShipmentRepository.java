package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
    Optional<Shipment> findByOutboundOrderId(Integer outboundOrderId);
}
