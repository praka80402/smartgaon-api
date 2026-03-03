package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/village-development")
@RequiredArgsConstructor
public class VillageDevelopmentController {

    private final VillageDevelopmentService service;

    /* ASSIGN PHASE */
    @PostMapping(consumes = "multipart/form-data")
    public VillageDevelopment assignPhase(
            @RequestParam Long villageId,
            @RequestParam Long developmentId,
            @RequestParam Integer progress,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) MultipartFile[] images
    ) {
        return service.assignPhase(
                villageId,
                developmentId,
                progress,
                remarks,
                images
        );
    }

    /* UPDATE WITH GALLERY */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public VillageDevelopment updateWithGallery(
            @PathVariable Long id,
            @RequestParam Integer progress,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) List<String> existingImages,
            @RequestParam(required = false) MultipartFile[] images
    ) {
        return service.updateWithGallery(
                id,
                progress,
                remarks,
                existingImages,
                images
        );
    }

    /* GET BY VILLAGE */
    @GetMapping("/village/{villageId}")
    public List<VillageDevelopment> getByVillage(@PathVariable Long villageId) {
        return service.getByVillage(villageId);
    }

    /* DELETE */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}