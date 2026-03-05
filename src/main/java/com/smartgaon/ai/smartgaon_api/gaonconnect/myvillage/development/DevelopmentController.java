package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/development")
@RequiredArgsConstructor
public class DevelopmentController {

    private final DevelopmentRepository repo;
    private final DevelopmentMasterRepository masterRepo;
    private final DevelopmentImageRepository imageRepo;
    private final DevelopmentService service;
    private final S3Service s3Service;

    /* ================= MASTER CREATE ================= */
    @PostMapping(value = "/master", consumes = "multipart/form-data")
    public DevelopmentMaster createMaster(
            @RequestParam String title,
            @RequestParam MultipartFile image
    ) throws IOException {

        String imageUrl = s3Service.uploadFile(image);

        DevelopmentMaster master = new DevelopmentMaster();
        master.setTitle(title);
        master.setImageUrl(imageUrl);

        return masterRepo.save(master);
    }

    /* ================= GET ALL MASTERS ================= */
    @GetMapping("/master")
    public List<DevelopmentMaster> getAllMasters() {
        return masterRepo.findAll();
    }

    /* ================= CREATE DEVELOPMENT ================= */
    @PostMapping(consumes = "multipart/form-data")
    public Development createDevelopment(
            @RequestParam Integer phaseNumber,
            @RequestParam Long masterId,
            @RequestParam String description,
            @RequestParam PhaseStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<MultipartFile> images
    ) throws IOException {

        DevelopmentMaster master = masterRepo.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Master not found"));

        Development dev = new Development();
        dev.setPhaseNumber(phaseNumber);
        dev.setMaster(master);
        dev.setDescription(description);
        dev.setStatus(status);

        if (startDate != null && !startDate.isEmpty())
            dev.setStartDate(LocalDate.parse(startDate));

        if (endDate != null && !endDate.isEmpty())
            dev.setEndDate(LocalDate.parse(endDate));

        Development savedDev = repo.save(dev);

        if (images != null) {
            for (MultipartFile file : images) {
                String imageUrl = s3Service.uploadFile(file);

                DevelopmentImage img = new DevelopmentImage();
                img.setImageUrl(imageUrl);
                img.setDevelopment(savedDev);

                imageRepo.save(img);
            }
        }

        return repo.findById(savedDev.getId()).get();
    }

    /* ================= GET ALL ================= */
    @GetMapping
    public List<Development> getAll() {
        return repo.findAll();
    }

    /* ================= GET BY PHASE ================= */
    @GetMapping("/phase/{phaseNumber}")
    public List<Development> getByPhase(
            @PathVariable Integer phaseNumber) {
        return repo.findByPhaseNumberOrderByIdDesc(phaseNumber);
    }

    @GetMapping("/{id}")
    public Development getProjectById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    /* ================= UPDATE ================= */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Development update(
            @PathVariable Long id,
            @RequestParam Integer phaseNumber,
            @RequestParam Long masterId,
            @RequestParam String description,
            @RequestParam PhaseStatus status,
            @RequestParam(required = false) List<MultipartFile> images
    ) throws IOException {

        Development existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Development not found"));

        DevelopmentMaster master = masterRepo.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Master not found"));

        existing.setPhaseNumber(phaseNumber);
        existing.setMaster(master);
        existing.setDescription(description);
        existing.setStatus(status);

        if (images != null && !images.isEmpty()) {

            if (images.size() > 24)
                throw new RuntimeException("Maximum 24 images allowed");

            for (MultipartFile file : images) {

                String imageUrl = s3Service.uploadFile(file);

                DevelopmentImage img = new DevelopmentImage();
                img.setImageUrl(imageUrl);
                img.setDevelopment(existing);

                imageRepo.save(img);
            }
        }

        return repo.save(existing);
    }
    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Development deleted successfully";
    }
}