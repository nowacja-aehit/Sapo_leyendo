package com.mycompany.sapo_leyendo.database;

// Zaktualizowane importy
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ZAKTUALIZOWANA Implementacja (Twoja "Realna Baza")
 * Zależna od SqlScriptRunner.java i modeli Product.java/User.java
 */
public class SqliteDatabaseConnection implements DatabaseInterface {

    private final String dbUrl;
    
    // ZAKTUALIZOWANE ŚCIEŻKI:
    // Zakładamy, że aplikacja jest uruchamiana z głównego folderu projektu,
    // a NetBeans/Maven poprawnie skopiuje te zasoby.
    // Używamy "src/main/java/" jako bazy dla ścieżek
    // (Dostosuj, jeśli Twoje pliki SQL są gdzie indziej, np. w "src/main/resources")
    private static final String Schema_RES = "/database/CreateDB_sqlite.sql";
    private static final String SEED_RES = "/database/FillDatabase_sqlite.sql";


    public SqliteDatabaseConnection(String dbName) {
        // Stworzyłem ci tu katalog na plik bazy, żeby nie lądował luzem.
        java.io.File dataDir = new java.io.File("data");
        if (!dataDir.exists()){
            dataDir.mkdirs();
        }
        this.dbUrl = "jdbc:sqlite:data/" + dbName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
            stmt.execute("PRAGMA busy_timeout=5000"); // 5 sekund
        }
        return conn;
    }

    /**
     * Wykonuje skrypt tworzący strukturę bazy (CreateDB_sqlite.sql).
     */
    @Override
    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            // Używamy nowej klasy SqlScriptRunner
            SqlScriptRunner.executeScriptFromResource(conn, Schema_RES);
        } catch (IOException e) {
            System.err.println("Nie można wczytać pliku schema SQL: " + e.getMessage());
            throw new SQLException("Błąd I/O podczas inicjalizacji bazy", e);
        }
    }

    /**
     * Wykonuje skrypt wypełniający bazę danymi (FillDatabase_sqlite.sql).
     */
    @Override
    public void seedDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            // Używamy nowej klasy SqlScriptRunner
            SqlScriptRunner.executeScriptFromResource(conn, SEED_RES);
        } catch (IOException e) {
            System.err.println("Nie można wczytać pliku seeder SQL: " + e.getMessage());
            throw new SQLException("Błąd I/O podczas wypełniania bazy", e);
        }
    }

    // --- Zaktualizowane metody CRUD dla Produktów ---

    @Override
    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO Products(sku, name, description, id_category, id_base_uom, weight_kg, length_cm, width_cm, height_cm) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.sku());
            pstmt.setString(2, product.name());
            pstmt.setString(3, product.description());
            pstmt.setObject(4, product.idCategory()); 
            pstmt.setInt(5, product.idBaseUom());
            pstmt.setObject(6, product.weightKg());
            pstmt.setObject(7, product.lengthCm());
            pstmt.setObject(8, product.widthCm());
            pstmt.setObject(9, product.heightCm());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Optional<Product> getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM Products WHERE id_product = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToProduct(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> getProductBySku(String sku) throws SQLException {
        String sql = "SELECT * FROM Products WHERE sku = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToProduct(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT * FROM Products";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public void updateProduct(Product product) throws SQLException {
        String sql = """
            UPDATE Products SET 
                sku = ?, name = ?, description = ?, id_category = ?, id_base_uom = ?, 
                weight_kg = ?, length_cm = ?, width_cm = ?, height_cm = ? 
            WHERE id_product = ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.sku());
            pstmt.setString(2, product.name());
            pstmt.setString(3, product.description());
            pstmt.setObject(4, product.idCategory());
            pstmt.setInt(5, product.idBaseUom());
            pstmt.setObject(6, product.weightKg());
            pstmt.setObject(7, product.lengthCm());
            pstmt.setObject(8, product.widthCm());
            pstmt.setObject(9, product.heightCm());
            pstmt.setInt(10, product.id()); // Klauzula WHERE
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM Products WHERE id_product = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // --- Nowe metody (do testowania, czy dane się wczytały) ---

    @Override
    public Optional<User> getUserByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM Users WHERE login = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(
                    rs.getInt("id_user"),
                    rs.getString("login"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getBoolean("is_active")
                ));
            }
        }
        return Optional.empty();
    }

    @Override
    public int getLocationCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Locations";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // --- Prywatna metoda pomocnicza ---
    
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id_product"),
            rs.getString("sku"),
            rs.getString("name"),
            rs.getString("description"),
            (Integer) rs.getObject("id_category"), 
            rs.getInt("id_base_uom"),
            (Double) rs.getObject("weight_kg"),
            (Double) rs.getObject("length_cm"),
            (Double) rs.getObject("width_cm"),
            (Double) rs.getObject("height_cm")
        );
    }
}