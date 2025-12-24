package com.smartgaon.ai.smartgaon_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetch weather data from OpenWeatherMap (Free /forecast endpoint)
     */
    public JsonNode getWeatherData(double lat, double lon) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&appid=%s",
                lat, lon, apiKey.trim()
        );

        System.out.println("🌐 Fetching Weather Data From: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("✅ API Response Status: " + response.getStatusCode());
            return new ObjectMapper().readTree(response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("❌ API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("⚠️ Unexpected Error: " + e.getMessage());
            throw new RuntimeException("Weather fetch failed", e);
        }
    }

    /**
     * Detect potential disasters in next few forecast slots (next ~4 forecast entries = 12 hours)
     */
    public boolean checkForDisaster(JsonNode weatherData) {
        JsonNode list = weatherData.path("list");
        if (list.isMissingNode() || list.size() == 0) {
            return false;
        }

        for (int i = 0; i < Math.min(list.size(), 4); i++) { // Next ~12 hours (4x3h intervals)
            JsonNode forecast = list.get(i);
            double wind = forecast.path("wind").path("speed").asDouble();
            double rain = forecast.path("rain").path("3h").asDouble(0);
            String desc = forecast.path("weather").get(0).path("description").asText("").toLowerCase();

            if (wind > 60 || rain > 20 || desc.contains("storm") || desc.contains("cyclone") || desc.contains("rain")) {
                System.out.println("⚠️ Potential disaster detected: " + desc + " | wind=" + wind + " | rain=" + rain);
                return true;
            }
        }
        return false;
    }
}
