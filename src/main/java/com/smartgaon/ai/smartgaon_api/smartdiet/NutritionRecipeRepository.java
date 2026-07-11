package com.smartgaon.ai.smartgaon_api.smartdiet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionRecipeRepository extends JpaRepository<NutritionRecipe, Long> {

    // Only this user's searches, newest first
    List<NutritionRecipe> findByUserIdOrderByCreatedAtDesc(String userId);
}
