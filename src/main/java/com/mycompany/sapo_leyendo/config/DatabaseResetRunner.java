package com.mycompany.sapo_leyendo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes or resets the database at startup.
 * - For SQLite: Auto-initializes if database is empty, or resets when CLEARDATABASE=1
 * - For MySQL: Only resets when CLEARDATABASE=1 (DemoDataLoader handles initialization)
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class DatabaseResetRunner implements CommandLineRunner {

    private final DataSource dataSource;
    private final Environment environment;

    @Override
    public void run(String... args) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean sqliteProfile = !activeProfiles.contains("mysql");
        boolean shouldClear = shouldClearDatabase();
        
        if (shouldClear) {
            log.warn("CLEARDATABASE=1 detected. Clearing database before seeding. Aktywne profile: {}", activeProfiles);
            if (sqliteProfile) {
                resetAndSeedSqlite();
            } else {
                resetMySql();
            }
            return;
        }
        
        // For SQLite: check if database needs initialization (is empty)
        if (sqliteProfile && isDatabaseEmpty()) {
            log.info("📦 SQLite database is empty. Initializing with schema and seed data...");
            seedSqlite();
        } else if (sqliteProfile) {
            log.info("📦 SQLite database already initialized. Skipping seed.");
        }
    }

    private boolean shouldClearDatabase() {
        String flag = environment.getProperty("CLEARDATABASE",
                System.getenv().getOrDefault("CLEARDATABASE", "0"));
        if (flag == null) {
            flag = "0";
        }
        flag = flag.trim();
        log.info("CLEARDATABASE resolved to '{}'.", flag);
        return "1".equals(flag);
    }
    
    private boolean isDatabaseEmpty() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            // Check if Products table exists and has data
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products", Integer.class);
            return count == null || count == 0;
        } catch (Exception e) {
            // Table doesn't exist or other error - database needs initialization
            log.debug("Database check failed (likely empty): {}", e.getMessage());
            return true;
        }
    }

    private void resetMySql() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`"));
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        log.info("MySQL tables truncated.");
    }

    private void resetAndSeedSqlite() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("PRAGMA foreign_keys = OFF");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                String.class);
        tables.forEach(table -> jdbcTemplate.execute("DROP TABLE IF EXISTS \"" + table + "\""));
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");
        log.info("SQLite tables dropped.");
        seedSqlite();
    }

    private void seedSqlite() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("database/CreateDB_sqlite.sql"));
        populator.addScript(new ClassPathResource("database/FillDatabase_sqlite.sql"));
        populator.execute(dataSource);
        log.info("✅ SQLite schema and seed data loaded successfully.");
    }
}
