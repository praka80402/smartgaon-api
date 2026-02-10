package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import com.smartgaon.ai.smartgaon_api.s3.VideoSnsPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCategory;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.*;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TalentEntryService {

    private final TalentEntryRepository entryRepo;
    private final TalentCompetitionRepository compRepo;
    private final ReferenceNumberService referenceService;
    private final S3Service s3Service;
    private final VideoSnsPublisher snsPublisher;
    private final UserRepository userRepository;

    private final List<String> imageTypes = List.of("jpg", "jpeg", "png");
    private final List<String> videoTypes = List.of("mp4", "mov", "avi");

    public String participate(
    		Long userId, //
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
    	
    	// ⭐ Fetch ONLY pincode from user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

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
        entry.setUserPincode(user.getPincode());
        entry.setCategory(category);
        entry.setUserId(userId);
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


    public String participatewithSNS(
            Long userId,
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

        // 1️⃣ Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (isCompetition) {
            if (competitionId == null)
                throw new Exception("Competition ID is required.");

            compRepo.findById(competitionId)
                    .orElseThrow(() -> new Exception("Invalid competition"));
        }

        String ext = getExt(media);

        if (category == TalentCategory.ART && !imageTypes.contains(ext))
            throw new Exception("ART needs image file.");

        if (category != TalentCategory.ART && !videoTypes.contains(ext))
            throw new Exception("This category requires video file.");

        // 2️⃣ Upload profile image (small → immediate)
        String profileUrl = s3Service.uploadFile(profileImage);

        // 3️⃣ Upload RAW video (NOT optimized)
        String rawVideoUrl = s3Service.uploadFile(media);
        // e.g. s3://bucket/raw-videos/{uuid}.mp4

        // 4️⃣ Save DB entry (media not ready yet)
        TalentEntry entry = new TalentEntry();
        entry.setName(name);
        entry.setDob(dob);
        entry.setVillageOrArea(villageOrArea);
        entry.setPhone(phone);
        entry.setUserPincode(user.getPincode());
        entry.setCategory(category);
        entry.setUserId(userId);
        entry.setCompetition(isCompetition);
        entry.setCompetitionId(isCompetition ? competitionId : null);

        entry.setProfileImageUrl(profileUrl);
        entry.setMediaUrl(rawVideoUrl);           // raw for now
        entry.setMediaType(ext);
        entry.setProcessingStatus("PROCESSING");  // ⭐ NEW

        String ref = referenceService.generate();
entry.setReferenceNumber(ref);

// 🔥 THIS IS THE KEY FIX
entry.setMediaConvertGuid(ref);   

entryRepo.save(entry);


snsPublisher.publishVideoProcessingEvent(
    entry.getId(),       
    rawVideoUrl,
    category.name()
);

        return "Video uploaded. Processing started. Ref: " + ref;
    }


    private String getExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
    public Page<TalentEntry> getFeed(TalentCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return entryRepo.findByCategory(category, pageable);
    }
    
    public List<TalentCategory> getAllCategories(
            TalentCategory first,
            Long userId
    ) {

        List<TalentCategory> categories =
            entryRepo.findVisibleCategories(userId);

        if (first != null && categories.contains(first)) {
            categories.remove(first);
            categories.add(0, first);
        }

        return categories;
    }

 

    
//    public Page<TalentEntry> getFeed(
//            TalentCategory category,
//            int page,
//            int size,
//            Long userId
//    ) {
//        Pageable pageable =
//            PageRequest.of(page, size, Sort.by("createdAt").descending());
//
//        // If user not logged in → normal feed
//        if (userId == null) {
//            return entryRepo.findByCategory(category, pageable);
//        }
//
//        // Logged-in user → hide reported posts
//        return entryRepo.findByCategoryWithoutReported(category, userId, pageable);
//    }

    public Page<TalentEntry> getFeed(
            TalentCategory category,
            int page,
            int size,
            Long userId
    ) {

        Pageable pageable =
            PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Not logged in
        if (userId == null) {
            return entryRepo.findByCategoryAndBlockedFalse(category, pageable);
        }

        // Logged in (with report logic)
        return entryRepo.findFeedWithReportLogic(category, userId, pageable);
    }

    public List<Map<String, Object>> getTopLikedCategories() {

        List<Object[]> data = entryRepo.findTopLikedCategories();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {

            Map<String, Object> map = new HashMap<>();

            map.put("category", row[0]);
            map.put("likes", row[1]);

            result.add(map);
        }

        return result;
    }

    public Page<TalentEntry> getAllReels(
            int page,
            int size,
            Long userId
    ) {

        Pageable pageable =
            PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Not logged in
        if (userId == null) {
            return entryRepo.findAllVisible(pageable);
        }

        // Logged in
        return entryRepo.findAllForUser(userId, pageable);
    }
    
 // ---------------- DELETE ENTRY ----------------
    public void deleteEntry(Long entryId, Long userId) throws Exception {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new Exception("Entry not found"));

        // Only owner can delete
        if (!entry.getUserId().equals(userId)) {
            throw new Exception("You are not authorized to delete this entry");
        }

        // Delete files from S3
        if (entry.getProfileImageUrl() != null)
            s3Service.deleteFile(entry.getProfileImageUrl());

        if (entry.getMediaUrl() != null)
            s3Service.deleteFile(entry.getMediaUrl());

        // Delete DB record
        entryRepo.delete(entry);
    }

    
 // ---------------- SHARE LINK ----------------
    public Map<String, String> getShareLink(Long entryId) throws Exception {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new Exception("Entry not found"));

        String shareUrl = "https://smartgaonai.com/talent/" + entry.getId();

        Map<String, String> result = new HashMap<>();
        result.put("shareUrl", shareUrl);
        result.put("name", entry.getName());
        result.put("category", entry.getCategory().name());

        return result;
    }



}
