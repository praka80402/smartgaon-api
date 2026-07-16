package com.smartgaon.ai.smartgaon_api.smartdiet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * All request/response DTOs for the Swaastha integration, kept in one file.
 * Use as NutritionDto.GenerateRequest, NutritionDto.SwaasthResponse, etc.
 */
public class NutritionDto {

    private NutritionDto() {}

    /** Body from the SmartGaon frontend, forwarded to Swaastha /generate. */
    public static class GenerateRequest {
        private List<String> ingredients;
        private String lang;                 // "en" | "hi" | "mr"; defaults to "en"
        private String userId;               // logged-in user id (for per-user history)

        public List<String> getIngredients() { return ingredients; }
        public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    /** Maps the Swaastha /generate response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SwaasthResponse {
        private List<Recipe> recipes;
        @JsonProperty("recipes_raw")
        private List<Object> recipesRaw;

        public List<Recipe> getRecipes() { return recipes; }
        public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }
        public List<Object> getRecipesRaw() { return recipesRaw; }
        public void setRecipesRaw(List<Object> recipesRaw) { this.recipesRaw = recipesRaw; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipe {
        private String id;
        private String name;
        private String difficulty;
        @JsonProperty("prep_time_mins") private Integer prepTimeMins;
        @JsonProperty("cook_time_mins") private Integer cookTimeMins;
        @JsonProperty("serving_size")   private Integer servingSize;
        private List<Ingredient> ingredients;
        private List<String> instructions;
        @JsonProperty("nutrition_highlights") private List<String> nutritionHighlights;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
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
        public List<Ingredient> getIngredients() { return ingredients; }
        public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
        public List<String> getInstructions() { return instructions; }
        public void setInstructions(List<String> instructions) { this.instructions = instructions; }
        public List<String> getNutritionHighlights() { return nutritionHighlights; }
        public void setNutritionHighlights(List<String> n) { this.nutritionHighlights = n; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ingredient {
        private String name;
        private String quantity;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
    }

    /** Maps the Swaastha /extract-ingredients response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractResponse {
        private List<String> ingredients;
        public List<String> getIngredients() { return ingredients; }
        public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    }
}
