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

    public VillageDevelopment assignPhase(
            Long villageId,
            Long developmentId,
            Integer progress,
            String remarks,
            MultipartFile[] images
    ) {

        if (progress < 0 || progress > 100)
            throw new RuntimeException("Progress must be between 0 and 100");

        Village village = villageRepo.findById(villageId).orElseThrow();
        Development development = developmentRepo.findById(developmentId).orElseThrow();

        VillageDevelopment vd = new VillageDevelopment();
        vd.setVillage(village);
        vd.setDevelopment(development);
        vd.setProgressPercent(progress);
        vd.setRemarks(remarks);

        List<String> uploadedUrls = new ArrayList<>();

        if (images != null) {
            if (images.length > 20)
                throw new RuntimeException("Maximum 20 images allowed");

            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    uploadedUrls.add(s3Service.uploadFile(img));
                }
            }
        }

        vd.setGalleryImages(uploadedUrls);

        return repo.save(vd);
    }
    /* UPDATE PROGRESS */
    public VillageDevelopment updateProgress(
            Long id,
            Integer progress,
            String remarks
    ) {

        if (progress < 0 || progress > 100)
            throw new RuntimeException("Progress must be between 0 and 100");

        VillageDevelopment vd = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        vd.setProgressPercent(progress);
        vd.setRemarks(remarks);

        return repo.save(vd);
    }

    /* GET ALL PHASES OF A VILLAGE */
    public List<VillageDevelopment> getByVillage(Long villageId) {
        return repo.findByVillageId(villageId);
    }

    /* DELETE */
    public void delete(Long id) {
        repo.deleteById(id);
    }
    
    public VillageDevelopment updateWithGallery(
            Long id,
            Integer progress,
            String remarks,
            List<String> existingImages,
            MultipartFile[] newImages
    ) {

        VillageDevelopment vd = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (progress < 0 || progress > 100)
            throw new RuntimeException("Progress must be between 0 and 100");

        vd.setProgressPercent(progress);
        vd.setRemarks(remarks);

        List<String> finalImages = new ArrayList<>();

        // Keep selected old images
        if (existingImages != null) {
            finalImages.addAll(existingImages);
        }

        // Upload new images
        if (newImages != null) {

            if (finalImages.size() + newImages.length > 20)
                throw new RuntimeException("Maximum 20 images allowed");

            for (MultipartFile img : newImages) {
                if (!img.isEmpty()) {
                    finalImages.add(s3Service.uploadFile(img));
                }
            }
        }

        vd.setGalleryImages(finalImages);

        return repo.save(vd);
    }


}
