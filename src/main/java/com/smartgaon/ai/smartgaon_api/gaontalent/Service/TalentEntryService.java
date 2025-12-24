package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCategory;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.*;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentEntryService {

    private final TalentEntryRepository entryRepo;
    private final TalentCompetitionRepository compRepo;
    private final ReferenceNumberService referenceService;
    private final S3Service s3Service;

    private final List<String> imageTypes = List.of("jpg", "jpeg", "png");
    private final List<String> videoTypes = List.of("mp4", "mov", "avi");

    public String participate(
            String name,
            LocalDate dob,
            String villageOrArea,
            String phone,
            TalentCategory category,
            Long competitionId,
            boolean isCompetition,
            MultipartFile profileImage,
            MultipartFile media
    ) throws Exception {

        if (isCompetition) {
            if (competitionId == null) {
                throw new Exception("Competition ID is required.");
            }

            compRepo.findById(competitionId)
                    .orElseThrow(() -> new Exception("Invalid competition"));
        }

        String ext = getExt(media);

        if (category == TalentCategory.ART && !imageTypes.contains(ext))
            throw new Exception("ART needs image file.");

        if (category != TalentCategory.ART && !videoTypes.contains(ext))
            throw new Exception("This category requires video file.");

        String profileUrl = s3Service.uploadFile(profileImage);
        String mediaUrl = s3Service.uploadFile(media);

        TalentEntry entry = new TalentEntry();
        entry.setName(name);
        entry.setDob(dob);                      // ⭐ NEW
        entry.setVillageOrArea(villageOrArea); 
        entry.setPhone(phone);
        entry.setCategory(category);

        entry.setCompetition(isCompetition);   // NEW
        entry.setCompetitionId(isCompetition ? competitionId : null);

        entry.setProfileImageUrl(profileUrl);
        entry.setMediaUrl(mediaUrl);
        entry.setMediaType(ext);

        String ref = referenceService.generate();
        entry.setReferenceNumber(ref);

        entryRepo.save(entry);

        return "Thank you for participating! Your reference number is: " + ref;
    }


    private String getExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
    public Page<TalentEntry> getFeed(TalentCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return entryRepo.findByCategory(category, pageable);
    }

}
