package com.mycompany.sapo_leyendo.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA Converter dla SQLite: Konwertuje LocalDateTime na TEXT (format 'YYYY-MM-DD HH:MM:SS')
 * SQLite nie ma natywnego typu DATETIME, więc używamy TEXT z STRFTIME.
 */
@Converter(autoApply = true)
public class LocalDateTimeStringConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isEmpty()) {
            return null;
        }
        try {
            // Handle various SQLite date formats
            if (dbValue.contains(".")) {
                // Format: 2025-12-10 10:00:00.000
                dbValue = dbValue.split("\\.")[0];
            }
            return LocalDateTime.parse(dbValue, FORMATTER);
        } catch (Exception e) {
            // Try ISO format as fallback
            try {
                return LocalDateTime.parse(dbValue);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
