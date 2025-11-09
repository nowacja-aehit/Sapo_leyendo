package com.mycompany.sapo_leyendo.model;

/**
 * NOWY Model Produktu (jako `record` Java).
 * Pola pasują do tabeli `Products` z Twojego pliku SQL.
 */
public record Product(
    int id,
    String sku,
    String name,
    String description,
    Integer idCategory, // Może być NULL
    int idBaseUom,
    Double weightKg, // Może być NULL
    Double lengthCm,
    Double widthCm,
    Double heightCm
) {
    /**
     * Dodatkowy konstruktor do tworzenia nowych produktów (bez ID).
     */
    public Product(String sku, String name, String description, Integer idCategory, int idBaseUom, Double weightKg, Double lengthCm, Double widthCm, Double heightCm) {
        this(0, sku, name, description, idCategory, idBaseUom, weightKg, lengthCm, widthCm, heightCm);
    }
}