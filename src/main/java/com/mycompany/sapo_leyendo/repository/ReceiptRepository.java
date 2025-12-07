package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {
}
