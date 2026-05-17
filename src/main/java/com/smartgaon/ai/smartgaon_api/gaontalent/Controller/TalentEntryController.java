package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCategory;
import com.smartgaon.ai.smartgaon_api.gaontalent.Service.TalentEntryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gaon-talent")  
@RequiredArgsConstructor
public class TalentEntryController {

    private final TalentEntryService service;

    // ---------------- PARTICIPATE API ----------------
    @PostMapping(value = "/participate", consumes = "multipart/form-data")
    public ResponseEntity<?> participate(
            @RequestParam Long userId,
            @RequestParam String name,
            @RequestParam String dob,             
            @RequestParam String villageOrArea, 
            @RequestParam String phone,
            @RequestParam TalentCategory category,
            @RequestParam(required = false) Long competitionId,
            @RequestParam boolean isCompetition,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(required = false) String profileImageUrl,
            @RequestParam(required = false) MultipartFile mediaFile,
            @RequestParam(required = false) String mediaUrl
    ) throws Exception {

        return ResponseEntity.ok(
                service.participate(userId, name, LocalDate.parse(dob), villageOrArea, phone, category, competitionId,
                        isCompetition, profileImage, profileImageUrl, mediaFile, mediaUrl)
        );
    }


    @PostMapping(value = "/partiSns", consumes = "multipart/form-data")
    public ResponseEntity<?> participateWithSns(
            @RequestParam Long userId,
            @RequestParam String name,
            @RequestParam String dob,
            @RequestParam String villageOrArea,
            @RequestParam String phone,
            @RequestParam TalentCategory category,
            @RequestParam(required = false) Long competitionId,
            @RequestParam boolean isCompetition,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(required = false) String profileImageUrl,
            @RequestParam(required = false) MultipartFile mediaFile,
            @RequestParam(required = false) String mediaUrl
    ) throws Exception {

        return ResponseEntity.ok(
                service.participatewithSNS(userId, name, LocalDate.parse(dob), villageOrArea, phone, category,
                        competitionId, isCompetition, profileImage, profileImageUrl, mediaFile, mediaUrl)
        );
    }

    // ---------------- FEED API (Category wise) ----------------

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam TalentCategory category,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(
            service.getFeed(category, page, size, userId)
        );
    }
 

    
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestParam(required = false) TalentCategory first,
            @RequestParam(required = false) Long userId
    ) {

        return ResponseEntity.ok(
            service.getAllCategories(first, userId)
        );
    }

    
    @GetMapping("/categories/top-liked")
    public ResponseEntity<?> topLikedCategories() {

        return ResponseEntity.ok(
            service.getTopLikedCategories()
        );
    }

    @GetMapping("/feed/all")
    public ResponseEntity<?> getAllReels(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Long userId
    ) {

        return ResponseEntity.ok(
            service.getAllReels(page, size, userId)
        );
    }

    @GetMapping({"/feed/my", "/feed/my/{userId}"})
    public ResponseEntity<?> getMyReels(
            @PathVariable(required = false) Long userId,
            @RequestParam(value = "userId", required = false) Long userIdParam,
            @RequestParam int page,
            @RequestParam int size
    ) {

        Long resolvedUserId = userId != null ? userId : userIdParam;
        if (resolvedUserId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }

        return ResponseEntity.ok(
            service.getMyReels(resolvedUserId, page, size)
        );
    }

 // ---------------- DELETE API ----------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntry(
            @PathVariable Long id,
            @RequestParam Long userId
    ) throws Exception {

        service.deleteEntry(id, userId);

        return ResponseEntity.ok("Entry deleted successfully.");
    }
    
 // ---------------- SHARE API ----------------
    @GetMapping("/share/{id}")
    public ResponseEntity<?> shareEntry(
            @PathVariable Long id
    ) throws Exception {

        return ResponseEntity.ok(
                service.getShareLink(id)
        );
    }

}
