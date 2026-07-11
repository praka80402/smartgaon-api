package com.smartgaon.ai.smartgaon_api.smartdiet;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One recipe returned by Swaastha AI, stored in SmartGaon's own table.
 * Nested lists are kept as JSON text so everything fits in a single table.
 * NOTE: On Spring Boot 2.x change "jakarta.persistence" to "javax.persistence".
 */
@Entity
@Table(name = "nutrition_recipe")
public class NutritionRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id")
    private String recipeId;                 // e.g. "rec-a1b2c3d4"

    @Column(name = "user_id")
    private String userId;                   // who generated this search

    private String name;
    private String difficulty;

    @Column(name = "prep_time_mins")
    private Integer prepTimeMins;

    @Column(name = "cook_time_mins")
    private Integer cookTimeMins;

    @Column(name = "serving_size")
    private Integer servingSize;

    @Column(name = "ingredients_json", columnDefinition = "TEXT")
    private String ingredientsJson;          // JSON array of {name, quantity}

    @Column(name = "instructions_json", columnDefinition = "TEXT")
    private String instructionsJson;         // JSON array of strings

    @Column(name = "nutrition_highlights_json", columnDefinition = "TEXT")
    private String nutritionHighlightsJson;  // JSON array of strings

    @Column(name = "input_ingredients", length = 1000)
    private String inputIngredients;         // what the user submitted

    private String lang;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getPrepTimeMins() { return prepTimeMins; }
    public void setPrepTimeMins(Integer prepTimeMins) { this.prepTimeMins = prepTimeMins; }
    public Integer getCookTimeMins() { return cookTimeMins; }
    public void setCookTimeMins(Integer cookTimeMins) { this.cookTimeMins = cookTimeMins; }
    public Integer getServingSize() { return servingSize; }
    public void setServingSize(Integer servingSize) { this.servingSize = servingSize; }
    public String getIngredientsJson() { return ingredientsJson; }
    public void setIngredientsJson(String ingredientsJson) { this.ingredientsJson = ingredientsJson; }
    public String getInstructionsJson() { return instructionsJson; }
    public void setInstructionsJson(String instructionsJson) { this.instructionsJson = instructionsJson; }
    public String getNutritionHighlightsJson() { return nutritionHighlightsJson; }
    public void setNutritionHighlightsJson(String s) { this.nutritionHighlightsJson = s; }
    public String getInputIngredients() { return inputIngredients; }
    public void setInputIngredients(String inputIngredients) { this.inputIngredients = inputIngredients; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
