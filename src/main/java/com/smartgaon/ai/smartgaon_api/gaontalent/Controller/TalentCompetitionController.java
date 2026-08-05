package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCompetition;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentCompetitionRepository;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/gaon-talent/competition")
@RequiredArgsConstructor
@Slf4j
public class TalentCompetitionController {

    private final TalentCompetitionRepository repo;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TalentCompetition competition) {
        return ResponseEntity.ok(repo.save(competition));
    }

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/active-upcoming")
    public ResponseEntity<?> getActiveAndUpcoming() {
        try {
            return ResponseEntity.ok(repo.findActiveAndUpcomingCompetitions(LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Error fetching active and upcoming competitions", e);
            throw e;
        }
    }
}




