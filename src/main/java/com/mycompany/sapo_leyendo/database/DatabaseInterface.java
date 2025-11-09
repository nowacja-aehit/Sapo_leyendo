/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.sapo_leyendo.database;

/**
 * Interfejs definiujący kontrakt dla modułu bazy danych.
 * Dzięki temu w przyszłości będziesz mógł łatwo podmienić SQLite
 * na inną bazę (np. PostgreSQL) tworząc nową klasę implementującą ten interfejs,
 * bez zmiany reszty aplikacji.
 *
 * @author j0now (i Gemini)
 */
public interface DatabaseInterface {

    /**
     * Nawiązuje połączenie z bazą danych.
     */
    void connect();

    /**
     * Zamyka połączenie z bazą danych.
     */
    void disconnect();

    /**
     * Tworzy początkowy schemat bazy danych (wszystkie tabele),
     * jeśli jeszcze nie istnieje.
     */
    void createInitialSchema();

    // === Przykłady metod dla modułów logicznych (do implementacji później) ===
    
    // Poniższe metody są na razie tylko przykładami.
    // W miarę rozwoju projektu będziesz dodawać tu więcej metod,
    // np. do pobierania, aktualizowania i usuwania danych.

    /**
     * Przykład: Dodaje nowy produkt do katalogu.
     * (Do zaimplementowania w przyszłości)
     * @param sku Unikalny kod SKU produktu
     * @param name Nazwa produktu
     */
    // void addProduct(String sku, String name);

    /**
     * Przykład: Znajduje lokalizację po jej kodzie.
     * (Do zaimplementowania w przyszłości)
     * @param locationCode Kod lokalizacji (np. "A-01-01-A")
     * @return Obiekt reprezentujący lokalizację (np. Twoja klasa POJO Location)
     */
    // Object getLocationByCode(String locationCode); // Użyj swojej klasy modelu zamiast Object

    /**
     * Przykład: Aktualizuje stan magazynowy (serce WMS).
     * (Do zaimplementowania w przyszłości)
     * @param productId ID produktu
     * @param locationId ID lokalizacji
     * @param quantity Ilość do dodania (lub ujemna, by odjąć)
     */
    // boolean updateStock(int productId, int locationId, int quantity);
}