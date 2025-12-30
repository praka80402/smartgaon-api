package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import com.smartgaon.ai.smartgaon_api.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/villages")
@RequiredArgsConstructor
public class VillageController {

    private final VillageService service;
    private final S3Service s3Service;

    /** =======================
     * GET ALL (everyone can view)
     * ======================= */
    @GetMapping
    public ResponseEntity<?> getAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        VillageDTO dto = service.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(dto);
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchVillages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state
    ) {
        return ResponseEntity.ok(service.search(page, size, name, city, state));
    }


}
