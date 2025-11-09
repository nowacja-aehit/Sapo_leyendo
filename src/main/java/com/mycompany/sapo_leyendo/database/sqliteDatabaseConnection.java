/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sapo_leyendo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Konkretna implementacja interfejsu bazy danych używająca SQLite.
 *
 * @author j0now (i Gemini)
 */
public class sqliteDatabaseConnection implements DatabaseInterface {

    // Ścieżka do pliku bazy danych. Plik zostanie utworzony w głównym katalogu projektu.
    private static final String DB_URL = "jdbc:sqlite:wms_sapo_leyendo.db";

    private Connection connection;

    @Override
    public void connect() {
        try {
            // Sprawdzenie, czy sterownik JDBC jest dostępny
            Class.forName("org.sqlite.JDBC");
            
            // Zamknięcie istniejącego połączenia, jeśli jest otwarte
            if (connection != null && !connection.isClosed()) {
                disconnect();
            }
            
            // Nawiązanie nowego połączenia
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Połączono z bazą danych SQLite: " + DB_URL);
            
            // Włączenie obsługi kluczy obcych (ważne dla integralności danych)
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            
        } catch (SQLException e) {
            System.err.println("Błąd podczas łączenia z bazą danych: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Nie znaleziono sterownika SQLite JDBC!");
            System.err.println("Upewnij się, że dodałeś zależność Maven lub plik JAR do projektu.");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Rozłączono z bazą danych SQLite.");
            } catch (SQLException e) {
                System.err.println("Błąd podczas rozłączania z bazą danych: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createInitialSchema() {
        if (connection == null) {
            System.err.println("Brak połączenia z bazą danych. Wywołaj connect() przed createInitialSchema().");
            return;
        }

        // Tabele są zdefiniowane w tablicy, aby łatwo je tworzyć jedna po drugiej.
        // Używamy "CREATE TABLE IF NOT EXISTS", aby bezpiecznie uruchamiać tę metodę wielokrotnie.
        String[] tablesToCreate = {
            """
            CREATE TABLE IF NOT EXISTS Products (
                id_product INTEGER PRIMARY KEY AUTOINCREMENT,
                sku TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                description TEXT
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS Locations (
                id_location INTEGER PRIMARY KEY AUTOINCREMENT,
                location_code TEXT UNIQUE NOT NULL,
                location_type TEXT NOT NULL CHECK(location_type IN ('STORAGE', 'PICKING', 'RECEIVING_BAY', 'SHIPPING_BAY'))
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS InventoryStock (
                id_stock INTEGER PRIMARY KEY AUTOINCREMENT,
                id_product INTEGER NOT NULL,
                id_location INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity >= 0),
                batch_number TEXT,
                expiry_date TEXT,
                FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE CASCADE,
                FOREIGN KEY (id_location) REFERENCES Locations(id_location) ON DELETE RESTRICT
            );
            """,
            // Poniżej tabele na przyszłość (zgodnie z plikiem założeń)
            """
            CREATE TABLE IF NOT EXISTS Users (
                id_user INTEGER PRIMARY KEY AUTOINCREMENT,
                login TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('ADMIN', 'OPERATOR'))
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS WmsOrders (
                id_order INTEGER PRIMARY KEY AUTOINCREMENT,
                order_reference TEXT UNIQUE NOT NULL,
                status TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW', 'PICKING', 'PACKED', 'SHIPPED', 'CANCELLED'))
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS OrderLines (
                id_line INTEGER PRIMARY KEY AUTOINCREMENT,
                id_order INTEGER NOT NULL,
                id_product INTEGER NOT NULL,
                quantity_ordered INTEGER NOT NULL CHECK(quantity_ordered > 0),
                FOREIGN KEY (id_order) REFERENCES WmsOrders(id_order) ON DELETE CASCADE,
                FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT
            );
            """,
             """
            CREATE TABLE IF NOT EXISTS GoodsReceived (
                id_receipt INTEGER PRIMARY KEY AUTOINCREMENT,
                asn_reference TEXT,
                status TEXT NOT NULL DEFAULT 'EXPECTED' CHECK(status IN ('EXPECTED', 'RECEIVED', 'CANCELLED'))
            );
            """,
             """
            CREATE TABLE IF NOT EXISTS ReceiptLines (
                id_line INTEGER PRIMARY KEY AUTOINCREMENT,
                id_receipt INTEGER NOT NULL,
                id_product INTEGER NOT NULL,
                quantity_expected INTEGER NOT NULL,
                quantity_received INTEGER DEFAULT 0,
                FOREIGN KEY (id_receipt) REFERENCES GoodsReceived(id_receipt) ON DELETE CASCADE,
                FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT
            );
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : tablesToCreate) {
                stmt.execute(sql);
            }
            System.out.println("Schemat bazy danych został pomyślnie utworzony (lub już istniał).");
        } catch (SQLException e) {
            System.err.println("Błąd podczas tworzenia schematu bazy danych: " + e.getMessage());
            e.printStackTrace();
        }
    }
}