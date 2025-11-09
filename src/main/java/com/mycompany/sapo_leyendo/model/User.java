package com.mycompany.sapo_leyendo.model;

/**
 * NOWY Model Użytkownika (jako `record` Java).
 * Pola pasują do tabeli `Users` z Twojego pliku SQL.
 */
public record User(
    int id,
    String login,
    String firstName,
    String lastName,
    boolean isActive
) {
}