package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/villages")
@RequiredArgsConstructor
@CrossOrigin
public class VillageController {

    private final VillageService service;

    /* ================= GET ALL ================= */
    @GetMapping
    public List<VillageDTO> getAllVillages() {
        return service.findAll();
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    public VillageDTO getVillageById(@PathVariable Long id) {
        return service.findById(id);
    }

    /* ================= SEARCH WITH PAGINATION ================= */
    @GetMapping("/search")
    public Map<String, Object> searchVillages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state
    ) {
        return service.search(page, size, name, city, state);
    }

    /* ================= GET SMART GAON ================= */
    @GetMapping("/smart")
    public List<VillageDTO> getSmartVillages() {
        return service.getSmartVillages();
    }
}