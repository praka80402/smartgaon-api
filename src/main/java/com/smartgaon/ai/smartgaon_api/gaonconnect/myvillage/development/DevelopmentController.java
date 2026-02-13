package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/developments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DevelopmentController {

    private final DevelopmentRepository repo;
    private final S3Service s3Service;

    /* ---------------- CREATE WITH IMAGE ---------------- */


    /* ---------------- UPDATE WITH IMAGE ---------------- */
 

    /* ---------------- GET ALL ---------------- */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    /* ---------------- GET BY ID ---------------- */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ---------------- DELETE ---------------- */
 
}
