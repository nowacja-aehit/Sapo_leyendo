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
 * Resets the database at startup when CLEARDATABASE=1 is present in env vars.
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
        String clearFlag = System.getenv().getOrDefault("CLEARDATABASE", "0");
        if (!"1".equals(clearFlag)) {
            log.info("CLEARDATABASE flag not set to 1. Skipping database reset.");
            return;
        }

        log.warn("CLEARDATABASE=1 detected. Clearing database before seeding.");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        boolean mysqlProfile = Arrays.asList(environment.getActiveProfiles()).contains("mysql");

        if (mysqlProfile) {
            resetMySql(jdbcTemplate);
            // DemoDataLoader (mysql profile) will repopulate on empty tables
        } else {
            resetSqlite(jdbcTemplate);
            reseedSqlite();
        }
    }

    private void resetMySql(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`"));
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        log.info("MySQL tables truncated.");
    }

    private void resetSqlite(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("PRAGMA foreign_keys = OFF");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                String.class);
        tables.forEach(table -> jdbcTemplate.execute("DROP TABLE IF EXISTS \"" + table + "\""));
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");
        log.info("SQLite tables dropped.");
    }

    private void reseedSqlite() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("database/CreateDB_sqlite.sql"));
        populator.addScript(new ClassPathResource("database/FillDatabase_sqlite.sql"));
        populator.execute(dataSource);
        log.info("SQLite schema and seed data reloaded.");
    }
}
