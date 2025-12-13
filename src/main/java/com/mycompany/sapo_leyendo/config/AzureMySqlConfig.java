package com.mycompany.sapo_leyendo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguracja DataSource dla Azure MySQL.
 * Parsuje AZURE_MYSQL_CONNECTIONSTRING z formatu Azure na JDBC.
 * 
 * Format Azure: Database=...;Server=...;User Id=...;Password=...
 * Format JDBC:  jdbc:mysql://server:port/database
 */
@Configuration
@Profile("mysql")
public class AzureMySqlConfig {

    @Value("${AZURE_MYSQL_CONNECTIONSTRING:}")
    private String azureConnectionString;

    @Bean
    public DataSource dataSource() {
        Map<String, String> params = parseAzureConnectionString(azureConnectionString);
        
        String server = params.getOrDefault("Server", "localhost");
        String database = params.getOrDefault("Database", "sapo_leyendo");
        String userId = params.getOrDefault("User Id", "");
        String password = params.getOrDefault("Password", "");
        String port = params.getOrDefault("Port", "3306");
        
        // Buduj JDBC URL
        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=true&requireSSL=true&serverTimezone=UTC",
            server, port, database
        );
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(userId);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Konfiguracja pool'a
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        
        System.out.println("✅ Azure MySQL DataSource configured for: " + server + "/" + database);
        
        return dataSource;
    }

    /**
     * Parsuje Azure connection string do mapy parametrów.
     * Format: Key1=Value1;Key2=Value2;...
     */
    private Map<String, String> parseAzureConnectionString(String connectionString) {
        Map<String, String> params = new HashMap<>();
        
        if (connectionString == null || connectionString.isEmpty()) {
            System.err.println("⚠️ AZURE_MYSQL_CONNECTIONSTRING is empty!");
            return params;
        }
        
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            int eqIndex = part.indexOf('=');
            if (eqIndex > 0) {
                String key = part.substring(0, eqIndex).trim();
                String value = part.substring(eqIndex + 1).trim();
                params.put(key, value);
            }
        }
        
        return params;
    }
}
