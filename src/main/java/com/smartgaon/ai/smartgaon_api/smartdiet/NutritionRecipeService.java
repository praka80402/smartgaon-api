package com.smartgaon.ai.smartgaon_api.smartdiet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NutritionRecipeService {

    private final NutritionRecipeRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest;

    @Value("${swaasth.base-url:https://swaasth.supervity.ai/api/ai/recipes}")
    private String baseUrl;

    public NutritionRecipeService(NutritionRecipeRepository repository) {
        this.repository = repository;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(10_000);
        f.setReadTimeout(60_000);          // recipe generation can be slow
        this.rest = new RestTemplate(f);
    }

    /** Calls Swaastha /generate, saves each recipe into SmartGaon's table, returns saved rows. */
    public List<NutritionRecipe> generateAndSave(NutritionDto.GenerateRequest req) {
        if (req.getLang() == null || req.getLang().isBlank()) req.setLang("en");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NutritionDto.GenerateRequest> entity = new HttpEntity<>(req, headers);

        NutritionDto.SwaasthResponse body = rest.postForEntity(
                baseUrl + "/generate", entity, NutritionDto.SwaasthResponse.class).getBody();

        if (body == null || body.getRecipes() == null) return Collections.emptyList();

        String input = String.join(", ",
                req.getIngredients() == null ? Collections.emptyList() : req.getIngredients());

        List<NutritionRecipe> toSave = new ArrayList<>();
        for (NutritionDto.Recipe r : body.getRecipes()) {
            NutritionRecipe e = new NutritionRecipe();
            e.setRecipeId(r.getId());
            e.setUserId(req.getUserId());
            e.setName(r.getName());
            e.setDifficulty(r.getDifficulty());
            e.setPrepTimeMins(r.getPrepTimeMins());
            e.setCookTimeMins(r.getCookTimeMins());
            e.setServingSize(r.getServingSize());
            e.setIngredientsJson(toJson(r.getIngredients()));
            e.setInstructionsJson(toJson(r.getInstructions()));
            e.setNutritionHighlightsJson(toJson(r.getNutritionHighlights()));
            e.setInputIngredients(input);
            e.setLang(req.getLang());
            toSave.add(e);
        }
        return repository.saveAll(toSave);
    }

    /** Forwards an uploaded food image to Swaastha /extract-ingredients. */
    public List<String> extractIngredients(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", toResource(file));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        NutritionDto.ExtractResponse b = rest.postForEntity(
                baseUrl + "/extract-ingredients", entity, NutritionDto.ExtractResponse.class).getBody();
        return (b == null || b.getIngredients() == null) ? Collections.emptyList() : b.getIngredients();
    }

    /** Reads a single user's saved recipes back out of the table. */
    public List<NutritionRecipe> getSavedRecipes(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();   // no user -> no history (don't leak other users)
        }
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try { return mapper.writeValueAsString(value); }
        catch (Exception ex) { return null; }
    }

    private Resource toResource(MultipartFile file) {
        try {
            final String filename = file.getOriginalFilename();
            return new ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return filename; }
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read uploaded image", e);
        }
    }
}
