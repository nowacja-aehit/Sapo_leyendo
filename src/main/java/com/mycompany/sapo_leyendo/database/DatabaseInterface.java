package com.mycompany.sapo_leyendo.database;

// Importy dla nowych modeli
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * ZAKTUALIZOWANY INTERFEJS
 * Definiuje kontrakt dla operacji bazodanowych w systemie WMS.
 */
public interface DatabaseInterface {

    Connection getConnection() throws SQLException;
    void initializeDatabase() throws SQLException;
    void seedDatabase() throws SQLException;

    // --- Metody dla Produktów ---
    void addProduct(Product product) throws SQLException;
    Optional<Product> getProductById(int id) throws SQLException;
    Optional<Product> getProductBySku(String sku) throws SQLException;
    List<Product> getAllProducts() throws SQLException;
    void updateProduct(Product product) throws SQLException;
    void deleteProduct(int id) throws SQLException;

    // --- Metody dla Użytkowników (do testowania) ---
    Optional<User> getUserByLogin(String login) throws SQLException;

    // --- Metody dla Lokacji (do testowania) ---
    int getLocationCount() throws SQLException;
}