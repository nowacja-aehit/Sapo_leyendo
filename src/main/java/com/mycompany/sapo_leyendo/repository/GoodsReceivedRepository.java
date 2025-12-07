package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.GoodsReceived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceivedRepository extends JpaRepository<GoodsReceived, Integer> {
}
