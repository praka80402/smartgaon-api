package com.smartgaon.ai.smartgaon_api.schoolcompetition.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Starting database migration for unique constraints...");
        
        // 1. Drop old constraint if exists
        try {
            jdbcTemplate.execute("ALTER TABLE school_competition_submissions DROP INDEX uk_comp_student");
            log.info("Successfully dropped old index 'uk_comp_student' from database.");
        } catch (Exception e) {
            log.info("Index 'uk_comp_student' drop check: " + e.getMessage());
        }

        // 2. Add new unique index constraint if it doesn't exist
        try {
            jdbcTemplate.execute("ALTER TABLE school_competition_submissions ADD UNIQUE INDEX uk_comp_student_new (competition_id, group_category, class_grade, school_name, roll_number)");
            log.info("Successfully created new composite unique index 'uk_comp_student_new'.");
        } catch (Exception e) {
            log.info("Index 'uk_comp_student_new' creation check: " + e.getMessage());
        }
        
        // 3. One-time clear has already been executed. Skipping.
        
        log.info("Database migration completed.");
    }
}
