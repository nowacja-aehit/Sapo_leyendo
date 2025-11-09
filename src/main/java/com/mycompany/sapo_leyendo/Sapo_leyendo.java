package com.mycompany.sapo_leyendo;

// Poprawione importy, aby pasowały do Twojej struktury
import com.mycompany.sapo_leyendo.database.DatabaseInterface;
import com.mycompany.sapo_leyendo.database.sqliteDatabaseConnection;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.model.User;

import java.util.List;
import java.util.Optional;

/**
 * ZAKTUALIZOWANA klasa główna (zmieniona nazwa z Main).
 * Pokazuje, jak użyć interfejsu do stworzenia i wypełnienia bazy.
 */
public class Sapo_leyendo { // Zmieniona nazwa klasy
    
    private static final String DB_NAME = "sapo_wms_main.db";
    
    public static void main(String[] args) {
        try {
            // 1. Używamy tej samej, oddzielonej logiki
            // Zwróć uwagę na zmianę nazwy klasy implementacji
            DatabaseInterface db = new sqliteDatabaseConnection(DB_NAME);
            
            // 2. Tworzymy tabele (wykonuje CreateDB_sqlite.sql)
            System.out.println("Inicjalizacja bazy danych...");
            db.initializeDatabase();
            System.out.println("Struktura bazy danych utworzona.");

            // 3. Wypełniamy danymi (wykonuje FillDatabase_sqlite.sql)
            System.out.println("Wypełnianie bazy danymi...");
            db.seedDatabase();
            System.out.println("Baza danych wypełniona danymi testowymi.");

            // 4. Testujemy, czy dane zostały wczytane
            System.out.println("\n--- Testy odczytu ---");
            
            Optional<User> adminUser = db.getUserByLogin("admin");
            if (adminUser.isPresent()) {
                System.out.println("Znaleziono użytkownika: " + adminUser.get().firstName());
            } else {
                System.out.println("Nie znaleziono użytkownika 'admin'.");
            }

            Optional<Product> product = db.getProductBySku("LAP-DEL-XPS15");
            if (product.isPresent()) {
                System.out.println("Znaleziono produkt: " + product.get().name());
            } else {
                System.out.println("Nie znaleziono produktu 'LAP-DEL-XPS15'.");
            }
            
            List<Product> allProducts = db.getAllProducts();
            System.out.println("Liczba wszystkich produktów w bazie: " + allProducts.size());
            
            int locationCount = db.getLocationCount();
            System.out.println("Liczba lokacji w bazie: " + locationCount);

        } catch (Exception e) {
            System.err.println("Wystąpił krytyczny błąd:");
            e.printStackTrace();
        }
    }
}