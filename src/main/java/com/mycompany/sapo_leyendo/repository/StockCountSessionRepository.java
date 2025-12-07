package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.StockCountSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface StockCountSessionRepository extends JpaRepository<StockCountSession, UUID> {
}
