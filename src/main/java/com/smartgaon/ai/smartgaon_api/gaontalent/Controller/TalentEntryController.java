package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCategory;
import com.smartgaon.ai.smartgaon_api.gaontalent.Service.TalentEntryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gaon-talent")   // <-- FIXED BASE PATH
@RequiredArgsConstructor
public class TalentEntryController {

    private final TalentEntryService service;

    // ---------------- PARTICIPATE API ----------------
    @PostMapping(value = "/participate", consumes = "multipart/form-data")
    public ResponseEntity<?> participate(
            @RequestParam String name,
            @RequestParam String dob,             
            @RequestParam String villageOrArea, 
            @RequestParam String phone,
            @RequestParam TalentCategory category,
            @RequestParam(required = false) Long competitionId,
            @RequestParam boolean isCompetition,
            @RequestParam MultipartFile profileImage,
            @RequestParam MultipartFile media
    ) throws Exception {

        return ResponseEntity.ok(
                service.participate(name, LocalDate.parse(dob), villageOrArea,phone, category, competitionId,isCompetition, profileImage, media)
        );
    }

    // ---------------- FEED API (Category wise) ----------------
    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam TalentCategory category,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(service.getFeed(category, page, size));
    }
}
