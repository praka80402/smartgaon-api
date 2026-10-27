package com.smartgaon.ai.smartgaon_api.scheme.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.scheme.entity.Category;
import com.smartgaon.ai.smartgaon_api.scheme.entity.*;
import com.smartgaon.ai.smartgaon_api.scheme.service.UserSchemeService;
import com.smartgaon.ai.smartgaon_api.scheme.dto.InterestedRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/schemes")
@RequiredArgsConstructor
@CrossOrigin
public class UserSchemeController {

    private final UserSchemeService service;

    // ================= GET ALL CATEGORIES =================
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return service.getAllCategories();
    }

    // ================= CENTRAL SCHEMES =================
    @GetMapping("/central")
    public List<Scheme> getCentralSchemes(
            @RequestParam(required = false) Long categoryId
    ) {
        if (categoryId != null) {
            return service.getCentralSchemes(categoryId);
        }
        return service.getAllCentralSchemes();
    }
    
    // ================= SCHEME DETAIL =================
    @GetMapping("/{schemeId}")
    public Scheme getSchemeDetail(
            @PathVariable Long schemeId
    ) {
        return service.getSchemeDetail(schemeId);
    }

    // ================= STATE SCHEMES =================
    @GetMapping("/state")
    public List<Scheme> getStateSchemes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam State state
    ) {
        if (categoryId != null) {
            return service.getStateSchemesByState(categoryId, state);
        }
        return service.getAllStateSchemesByState(state);
    }
    
    // ================= INTERESTED =================
    @PostMapping("/{schemeId}/interested")
    public ResponseEntity<?> addInterested(
            @PathVariable Long schemeId,
            @RequestBody InterestedRequest request
    ) {
        try {
            service.addInterestedUser(schemeId, request);
            return ResponseEntity.ok("Interest added successfully");

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{schemeId}/interested/check")
    public boolean checkInterested(
            @PathVariable Long schemeId,
            @RequestParam String phone
    ) {
        return service.isAlreadyInterested(schemeId, phone);
    }

}
