package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySku(String sku);
}
