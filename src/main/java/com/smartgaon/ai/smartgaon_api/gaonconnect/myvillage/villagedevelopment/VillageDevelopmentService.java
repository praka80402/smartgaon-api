package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.Village;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.VillageRepository;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.Development;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.DevelopmentRepository;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class VillageDevelopmentService {


    private final VillageDevelopmentRepository repo;
    private final VillageRepository villageRepo;
    private final DevelopmentRepository developmentRepo;
    private final S3Service s3Service;

    /* ================= ASSIGN DEVELOPMENT ================= */

    public VillageDevelopment assignPhase(
            Long villageId,
            Long developmentId,
            Integer progress,
            String remarks,
            String videoUrl,
            MultipartFile[] images,
            MultipartFile[] reports
    ) {

        Village village = villageRepo.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Village not found"));

        Development development = developmentRepo.findById(developmentId)
                .orElseThrow(() -> new RuntimeException("Development not found"));

        VillageDevelopment vd = new VillageDevelopment();
        vd.setVillage(village);
        vd.setDevelopment(development);
        vd.setProgressPercent(progress);
        vd.setRemarks(remarks);
        vd.setVideoUrl(videoUrl);

        List<String> gallery = new ArrayList<>();
        List<String> reportUrls = new ArrayList<>();

        /* Upload Images */

        if (images != null && images.length > 0) {

            for (MultipartFile img : images) {

                if (img != null && !img.isEmpty()) {

                    String url = s3Service.uploadFile(img);
                    gallery.add(url);

                }
            }
        }

        /* Upload Reports */

        if (reports != null && reports.length > 0) {

            for (MultipartFile pdf : reports) {

                if (pdf != null && !pdf.isEmpty()) {

                    String url = s3Service.uploadFile(pdf);
                    reportUrls.add(url);

                }
            }
        }

        vd.setGalleryImages(gallery);
        vd.setReports(reportUrls);

        return repo.save(vd);
    }

    /* ================= UPDATE BY VILLAGE ================= */

    public VillageDevelopment updateByVillage(
            Long villageId,
            Long developmentId,
            Integer progress,
            String remarks,
            String videoUrl,
            List<String> existingImages,
            List<String> existingReports,
            MultipartFile[] images,
            MultipartFile[] reports
    ) {

        VillageDevelopment vd = repo
                .findByVillageIdAndDevelopmentId(villageId, developmentId)
                .orElseThrow(() -> new RuntimeException("Village development not found"));

        vd.setProgressPercent(progress);
        vd.setRemarks(remarks);
        vd.setVideoUrl(videoUrl);

        List<String> finalImages = new ArrayList<>();
        List<String> finalReports = new ArrayList<>();

        /* Keep Existing Images */

        if (existingImages != null && !existingImages.isEmpty()) {
            finalImages.addAll(existingImages);
        }

        /* Upload New Images */

        if (images != null && images.length > 0) {

            for (MultipartFile img : images) {

                if (img != null && !img.isEmpty()) {

                    String url = s3Service.uploadFile(img);
                    finalImages.add(url);

                }
            }
        }

        /* Keep Existing Reports */

        if (existingReports != null && !existingReports.isEmpty()) {
            finalReports.addAll(existingReports);
        }

        /* Upload New Reports */

        if (reports != null && reports.length > 0) {

            for (MultipartFile pdf : reports) {

                if (pdf != null && !pdf.isEmpty()) {

                    String url = s3Service.uploadFile(pdf);
                    finalReports.add(url);

                }
            }
        }

        vd.setGalleryImages(finalImages);
        vd.setReports(finalReports);

        return repo.save(vd);
    }

    /* ================= GET BY VILLAGE ================= */

    public List<VillageDevelopment> getByVillage(Long villageId) {

        return repo.findByVillageId(villageId);

    }

    /* ================= DELETE ================= */

    public void delete(Long id) {

        repo.deleteById(id);

    }

    /* ================= GET BY ID ================= */

    public VillageDevelopment getById(Long id) {

        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Village development not found"));

    }
}