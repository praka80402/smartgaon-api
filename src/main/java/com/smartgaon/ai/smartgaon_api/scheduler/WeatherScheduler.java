package com.smartgaon.ai.smartgaon_api.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartgaon.ai.smartgaon_api.service.WeatherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class WeatherScheduler {

    @Autowired
    private WeatherService weatherService;

   
    private final double mumbaiLat = 19.0760;
    private final double mumbaiLon = 72.8777;

    @Scheduled(fixedRate = 3600000)
    public void checkWeatherUpdates() {
        JsonNode weatherData = weatherService.getWeatherData(mumbaiLat, mumbaiLon);

        if (weatherService.checkForDisaster(weatherData)) {
            System.out.println("⚠️ ALERT: Severe weather expected within 4 hours!");
            
        } else {
            System.out.println("✅ Weather is normal at " + java.time.LocalTime.now());
        }
    }
}
