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

    /* ================= ASSIGN DEVELOPMENT ================= */

    @PostMapping(value = "/village/{villageId}", consumes = "multipart/form-data")
    public VillageDevelopment assignPhase(
            @PathVariable Long villageId,
            @RequestParam Long developmentId,
            @RequestParam Integer progress,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) MultipartFile[] images,
            @RequestParam(required = false) MultipartFile[] reports
    ) {

        return service.assignPhase(
                villageId,
                developmentId,
                progress,
                remarks,
                videoUrl,
                images,
                reports
        );
    }

    /* ================= UPDATE BY VILLAGE ================= */

    @PutMapping(value = "/village/{villageId}/{developmentId}", consumes = "multipart/form-data")
    public VillageDevelopment updateByVillage(
            @PathVariable Long villageId,
            @PathVariable Long developmentId,
            @RequestParam("progress") Integer progress,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "existingImages", required = false) List<String> existingImages,
            @RequestParam(value = "existingReports", required = false) List<String> existingReports,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "reports", required = false) MultipartFile[] reports
    ) {

        return service.updateByVillage(
                villageId,
                developmentId,
                progress,
                remarks,
                videoUrl,
                existingImages,
                existingReports,
                images,
                reports
        );
    }

    /* ================= GET BY VILLAGE ================= */

    @GetMapping("/village/{villageId}")
    public List<VillageDevelopment> getByVillage(@PathVariable Long villageId) {

        return service.getByVillage(villageId);

    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        service.delete(id);

    }

    /* ================= GET IMAGES ================= */

    @GetMapping("/{id}/images")
    public List<String> getImages(@PathVariable Long id) {

        VillageDevelopment vd = service.getById(id);
        return vd.getGalleryImages();

    }

    /* ================= GET VIDEO ================= */

    @GetMapping("/{id}/video")
    public String getVideo(@PathVariable Long id) {

        VillageDevelopment vd = service.getById(id);
        return vd.getVideoUrl();

    }

    /* ================= GET REPORTS ================= */

    @GetMapping("/{id}/reports")
    public List<String> getReports(@PathVariable Long id) {

        VillageDevelopment vd = service.getById(id);
        return vd.getReports();

    }
}