package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/*
 * PUBLIC read-only endpoints for admin-created (sg_village) villages.
 * Mapped under /api/villages/sg so it sits beside the existing public
 * /api/villages endpoints and inherits the same public access + CORS.
 */
@RestController
@RequestMapping("/api/villages/sg")
@RequiredArgsConstructor
@CrossOrigin
public class SgVillageController {

    private final SgVillageService service;

    /* Smart-gaon villages created in the new admin */
    @GetMapping("/smart")
    public List<SgVillageDTO> smart() {
        return service.getSmartVillages();
    }

    /* All new-admin villages */
    @GetMapping
    public List<SgVillageDTO> all() {
        return service.getAll();
    }

    /* Single new-admin village by id */
    @GetMapping("/{id}")
    public SgVillageDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }
}
