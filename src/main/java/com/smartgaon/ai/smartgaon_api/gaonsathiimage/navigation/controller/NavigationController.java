package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationRequest;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationResponse;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationRoute;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.service.NavigationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gaon-sathi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NavigationController {

    private final NavigationService navigationService;

    @PostMapping("/navigation")
    public ResponseEntity<NavigationResponse> navigate(
            @RequestBody NavigationRequest request) {

        return ResponseEntity.ok(
                navigationService.resolveNavigation(request));
    }

    @GetMapping("/modules")
    public ResponseEntity<List<NavigationRoute>> getModules() {

        return ResponseEntity.ok(
                navigationService.getAllModules());
    }

    @GetMapping("/module/{moduleCode}")
    public ResponseEntity<NavigationRoute> getModule(
            @PathVariable String moduleCode) {

        NavigationRoute route =
                navigationService.getModuleByCode(moduleCode);

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(route);
    }
}