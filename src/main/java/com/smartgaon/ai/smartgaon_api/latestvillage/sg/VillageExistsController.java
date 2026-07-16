package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public check used by the website's My Village page to decide whether to
 * show the "+ Create your village" banner/button.
 *
 *   GET /api/public/villages/exists?name=Sheikhpura&district=Patna&state=Bihar
 *   → { "exists": true }  or  { "exists": false }
 *
 * Matches directly against the villages TABLE (case-insensitive,
 * whitespace-trimmed), so the answer always reflects what the admin has
 * actually created.
 *
 * ⚠ VERIFY THE TABLE NAME: run `SHOW TABLES;` in MySQL. If your villages
 *   table is not "sg_village", change it in the SQL below (one word).
 *   Same for the column names if "district" is e.g. "city".
 */
@RestController
@RequestMapping("/api/public/villages")
@CrossOrigin
public class VillageExistsController {

    private final JdbcTemplate jdbc;

    public VillageExistsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/exists")
    public Map<String, Boolean> exists(@RequestParam String name,
                                       @RequestParam(required = false, defaultValue = "") String district,
                                       @RequestParam(required = false, defaultValue = "") String state) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM sg_village
                WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))
                  AND (? = '' OR LOWER(TRIM(district)) = LOWER(TRIM(?)))
                  AND (? = '' OR LOWER(TRIM(state)) = LOWER(TRIM(?)))
                """,
                Integer.class,
                name, district, district, state, state);

        return Map.of("exists", count != null && count > 0);
    }
}
