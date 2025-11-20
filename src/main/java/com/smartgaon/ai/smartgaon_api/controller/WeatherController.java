package com.smartgaon.ai.smartgaon_api.controller;



import com.fasterxml.jackson.databind.JsonNode;
import com.smartgaon.ai.smartgaon_api.service.WeatherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public ResponseEntity<JsonNode> getWeather(@RequestParam double lat, @RequestParam double lon) {
        JsonNode data = weatherService.getWeatherData(lat, lon);
        return ResponseEntity.ok(data);
    }
}

