package com.mycompany.sapo_leyendo.database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ZAKTUALIZOWANA KLASA NARZĘDZIOWA
 * Poprawka #3: Dodano warunek, aby nie wykonywać instrukcji,
 * które są tylko komentarzami (zaczynają się od "--").
 */
public class SqlScriptRunner {

    /**
     * Wykonuje skrypt SQL z pliku.
     * @param conn Aktywne połączenie z bazą danych
     * @param scriptFilePath Ścieżka do pliku .sql
     * @throws SQLException
     * @throws IOException
     */
    public static void executeScript(Connection conn, String scriptFilePath) throws SQLException, IOException {
        
        System.out.println("Wykonywanie skryptu: " + scriptFilePath);
        String sql = new String(Files.readAllBytes(Paths.get(scriptFilePath)), StandardCharsets.UTF_8);

        // Usuwamy komentarze jednoliniowe (ważne dla SQLite)
        // Ta linia jest nadal potrzebna, aby usunąć komentarze na końcu linii (np. "INSERT ...; -- komentarz")
        sql = sql.replaceAll("--.*?\r?\n", "");
        // Usuwamy puste linie, aby uniknąć błędów
        sql = sql.replaceAll("(?m)^\\s*\\r?\\n", "");
        
        // Dzielimy na pojedyncze instrukcje
        String[] statements = sql.split(";");

        // Połączenie będzie działać w domyślnym trybie auto-commit.
        
        for (String s : statements) {
            String statement = s.trim();
            
            // --- OSTATECZNA POPRAWKA ---
            // Sprawdzamy, czy instrukcja nie jest pusta ORAZ
            // czy nie jest TYLKO komentarzem (który mógł zostać po parsowaniu).
            if (!statement.isEmpty() && !statement.startsWith("--")) {
                
                // Try-with-resources dla każdego Statement
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(statement);
                } catch (SQLException e) {
                    System.err.println("KRYTYCZNY BŁĄD podczas wykonywania instrukcji:");
                    System.err.println("Instrukcja: " + statement);
                    System.err.println("Błąd: " + e.getMessage());
                    // Rzucamy błąd dalej, aby zatrzymać wykonywanie
                    throw e;
                }
            }
        }
        
        System.out.println("Skrypt wykonany pomyślnie (tryb auto-commit).");
    }
    /**
     * Wykonuje skrypt SQL z zasobu w classpath (np. /database/plik.sql)
     */
    public static void executeScriptFromResource(Connection conn, String resourcePath) throws SQLException, IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IOException("Podana ścieżka zasobu SQL jest pusta.");
        }

        System.out.println("Wykonywanie skryptu z zasobu: " + resourcePath);

        try (InputStream in = SqlScriptRunner.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Nie znaleziono zasobu SQL: " + resourcePath);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            String tempPath = writeTempAndReturnPath(sb.toString());

            try {
                executeScriptFromResource(conn, tempPath);
            } finally {
                Files.deleteIfExists(Paths.get(tempPath));
            }
        }
    }

    private static String writeTempAndReturnPath(String sqlText) throws IOException {
        var tmp = Files.createTempFile("sql_script_", ".sql");
        Files.writeString(tmp, sqlText, StandardCharsets.UTF_8);
        return tmp.toString();
    }

}