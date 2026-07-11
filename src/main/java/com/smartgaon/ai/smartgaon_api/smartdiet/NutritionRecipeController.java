package com.smartgaon.ai.smartgaon_api.smartdiet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** SmartGaon-facing endpoints. Lock the CORS origin to your domain before going live. */
@RestController
@RequestMapping("/api/smartgaon/nutrition")
@CrossOrigin(origins = "*")
public class NutritionRecipeController {

    private final NutritionRecipeService service;

    public NutritionRecipeController(NutritionRecipeService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<NutritionRecipe>> generate(@RequestBody NutritionDto.GenerateRequest request) {
        return ResponseEntity.ok(service.generateAndSave(request));
    }

    @PostMapping(value = "/extract-ingredients", consumes = "multipart/form-data")
    public ResponseEntity<List<String>> extractIngredients(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.extractIngredients(file));
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<NutritionRecipe>> getSaved(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(service.getSavedRecipes(userId));
    }
}
