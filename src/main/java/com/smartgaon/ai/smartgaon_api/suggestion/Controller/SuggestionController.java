package com.smartgaon.ai.smartgaon_api.suggestion.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.suggestion.Entity.Suggestion;
import com.smartgaon.ai.smartgaon_api.suggestion.Service.SuggestionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
// @CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService service;


    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Suggestion s) {
        if (s.getDescription() == null || s.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description cannot be empty"));
        }
        if (s.getPincode() == null || s.getPincode().trim().length() != 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid 6-digit pincode required"));
        }
        Suggestion saved = service.submit(s);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/by-pincode/{pincode}")
    public ResponseEntity<List<Suggestion>> byPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(service.getByPincode(pincode));
    }

    @GetMapping("/my/{phone}")
    public ResponseEntity<List<Suggestion>> mySuggestions(@PathVariable String phone) {
        return ResponseEntity.ok(service.getByPhone(phone));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Suggestion Not Found"));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Suggestion s) {
        try {
            Suggestion updated = service.update(id, s);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
