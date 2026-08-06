package com.smartgaon.ai.smartgaon_api.quick;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quick-services")
public class QuickServiceController {

    private final QuickServiceService service;

    public QuickServiceController(QuickServiceService service) {
        this.service = service;
    }

    @GetMapping("/active")
    public List<QuickService> getActive() {
        return service.getActiveServices();
    }
}

@RestController
@RequestMapping("/api/admin/quick-services")
@PreAuthorize("hasRole('ADMIN')")
class AdminQuickServiceController {

    private final QuickServiceService service;

    AdminQuickServiceController(QuickServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<QuickService> getAll() {
        return service.getAllServices();
    }

    @PostMapping
    public ResponseEntity<QuickService> create(@Valid @RequestBody QuickServiceRequest req) {
        QuickService created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody QuickServiceRequest req) {
        try {
            return ResponseEntity.ok(service.update(id, req));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.toggleActive(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Body: { "items": [ { "id": 3, "displayOrder": 0 }, { "id": 1, "displayOrder": 1 }, ... ] }
    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody ReorderRequest req) {
        service.reorder(req);
        return ResponseEntity.ok().build();
    }
}
