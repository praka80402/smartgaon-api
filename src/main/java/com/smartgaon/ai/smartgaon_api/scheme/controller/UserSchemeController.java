package com.smartgaon.ai.smartgaon_api.scheme.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.scheme.entity.Category;
import com.smartgaon.ai.smartgaon_api.scheme.entity.*;
import com.smartgaon.ai.smartgaon_api.scheme.service.UserSchemeService;

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
            @RequestParam Long categoryId
    ) {
        return service.getCentralSchemes(categoryId);
    }
    
 // ================= SCHEME DETAIL =================
    @GetMapping("/{schemeId}")
    public Scheme getSchemeDetail(
            @PathVariable Long schemeId
    ) {
        return service.getSchemeDetail(schemeId);
    }


    // ================= STATE SCHEMES =================
 // ================= STATE SCHEMES =================
    @GetMapping("/state")
    public List<Scheme> getStateSchemes(
            @RequestParam Long categoryId,
            @RequestParam State state
    ) {
        return service.getStateSchemesByState(categoryId, state);
    }

}
