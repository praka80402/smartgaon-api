package com.smartgaon.ai.smartgaon_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.exception.RateLimitExceededException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CareerGuideController {

    private static final int DAILY_LIMIT = 10;
    private final GroqQuestionService service;
    private final RedisCareerGuideService redisService;
    private final ObjectMapper mapper = new ObjectMapper();

    public CareerGuideController(GroqQuestionService service, RedisCareerGuideService redisService) {
        this.service = service;
        this.redisService = redisService;
    }

    @GetMapping("/guide")
    public GroqQuestionService.CareerGuideResponse guide(
            @RequestParam String field,
            @RequestParam(defaultValue = "0") int exp,
            @RequestParam(defaultValue = "en") String lang,
            @RequestHeader("X-IDENTIFIER") String identifier
    ) throws Exception {

        identifier = identifier.trim().toLowerCase();

        // ----- RATE LIMIT -----
        String redisKey = "guide:" + identifier + ":" + LocalDate.now() + ":count";
        boolean allowed = redisService.incrementAndCheckLimit(redisKey, DAILY_LIMIT);

        if (!allowed) {
            long used = redisService.getCount(redisKey);
            throw new RateLimitExceededException(DAILY_LIMIT, used);
        }


        // ----- CALL GROQ -----
        GroqQuestionService.CareerGuideResponse response =
                service.generateCareerGuide(field, exp, lang);

        // ----- STORE RAW JSON -----
        String json = mapper.writeValueAsString(response);
        redisService.saveGuideResponse(identifier, json);

        return response;
    }

    @GetMapping("/guide/history")
    public List<String> history(
            @RequestHeader("X-IDENTIFIER") String identifier
    ) {
        identifier = identifier.trim().toLowerCase();
        return redisService.getHistory(identifier);
    }

    @PostMapping("/guide/reset")
    public Map<String, Object> resetLimit(
            @RequestHeader("X-IDENTIFIER") String identifier
    ) {

        identifier = identifier.trim().toLowerCase();

        redisService.resetDailyLimit(identifier);

        return Map.of(
                "success", true,
                "message", "Daily career guide limit has been reset",
                "identifier", identifier
        );
    }

}

