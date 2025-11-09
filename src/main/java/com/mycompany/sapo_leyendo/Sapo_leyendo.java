/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.sapo_leyendo;

import com.mycompany.sapo_leyendo.database.DatabaseInterface;
import com.mycompany.sapo_leyendo.database.sqliteDatabaseConnection;

/**
 * Główna klasa aplikacji Sapo_leyendo WMS.
 *
 * @author j0now (i Gemini)
 */
public class Sapo_leyendo {

    public static void main(String[] args) {
        System.out.println("Uruchamianie Systemu WMS Sapo_leyendo...");

        // 1. Inicjalizacja modułu bazy danych
        // Używamy Interfejsu jako typu - to dobra praktyka (polimorfizm).
        // Aplikacja wie tylko, że ma do czynienia z "jakąś" bazą danych.
        DatabaseInterface dbHandler = new sqliteDatabaseConnection();

        try {
            // 2. Nawiązanie połączenia
            dbHandler.connect();

            // 3. Utworzenie tabel (jeśli nie istnieją)
            // Ten krok zapewnia, że aplikacja zawsze ma gotową strukturę bazy.
            dbHandler.createInitialSchema();

            // 4. Tutaj w przyszłości uruchomisz główny moduł aplikacji,
            //    np. moduł interfejsu użytkownika (UI).
            System.out.println("System gotowy do pracy. (W przyszłości tutaj uruchomisz UI)");
            // np. UILauncher.startUI(dbHandler);
            
            // ...
            // Aplikacja działa...
            // ...

        } catch (Exception e) {
            System.err.println("Wystąpił krytyczny błąd podczas uruchamiania aplikacji:");
            e.printStackTrace();
        } finally {
            // 5. Rozłączenie z bazą danych przy zamknięciu aplikacji.
            // (W prawdziwej aplikacji UI ten krok nastąpiłby przy zamykaniu okna)
            dbHandler.disconnect();
        }
    }
}