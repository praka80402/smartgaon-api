package com.smartgaon.ai.smartgaon_api.fertilizer.controller;

import com.smartgaon.ai.smartgaon_api.fertilizer.entity.CropFertilizerStandard;
import com.smartgaon.ai.smartgaon_api.fertilizer.entity.FertilizerPrice;
import com.smartgaon.ai.smartgaon_api.fertilizer.service.FertilizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/kisan-mitra/fertilizer", "/api/fertilizer"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FertilizerController {

    private final FertilizerService fertilizerService;

    /**
     * GET /api/kisan-mitra/fertilizer/prices
     * Returns real-time fertilizer rates, state subsidy details, and last updated timestamp.
     * Optional 'state' parameter to filter/adjust rates for a specific Indian state.
     */
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Object>> getPrices(
            @RequestParam(required = false) String state
    ) {
        return ResponseEntity.ok(fertilizerService.getPricesPayload(state));
    }

    /**
     * GET /api/kisan-mitra/fertilizer/states
     * Returns list of supported Indian states and their primary fertilizer marketing agencies.
     */
    @GetMapping("/states")
    public ResponseEntity<List<Map<String, Object>>> getSupportedStates() {
        return ResponseEntity.ok(fertilizerService.getSupportedStates());
    }

    /**
     * GET /api/kisan-mitra/fertilizer/crops
     * Returns list of all crops with certified ICAR RDF, stages, and categories.
     */
    @GetMapping("/crops")
    public ResponseEntity<List<CropFertilizerStandard>> getCrops(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state
    ) {
        return ResponseEntity.ok(fertilizerService.getCrops(category, state));
    }

    /**
     * GET /api/kisan-mitra/fertilizer/crops/{cropKey}
     * Returns specific crop details by key.
     */
    @GetMapping("/crops/{cropKey}")
    public ResponseEntity<CropFertilizerStandard> getCropByKey(@PathVariable String cropKey) {
        return fertilizerService.getCrop(cropKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/kisan-mitra/fertilizer/prices/{itemKey}
     * Updates an individual fertilizer price.
     */
    @PostMapping("/prices/{itemKey}")
    public ResponseEntity<FertilizerPrice> updatePrice(
            @PathVariable String itemKey,
            @RequestBody Map<String, Object> body
    ) {
        Double newPrice = Double.valueOf(body.get("price").toString());
        String source = body.containsKey("source") ? body.get("source").toString() : null;
        return ResponseEntity.ok(fertilizerService.updatePrice(itemKey, newPrice, source));
    }

    /**
     * POST /api/kisan-mitra/fertilizer/sync
     * Triggers on-demand rate synchronization.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync() {
        fertilizerService.syncOfficialRates();
        return ResponseEntity.ok(Map.of("success", true, "message", "Fertilizer rates synchronized successfully"));
    }
}
