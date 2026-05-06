package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

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

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TalentEntryService {

    private final TalentEntryRepository entryRepo;
    private final TalentCompetitionRepository compRepo;
    private final ReferenceNumberService referenceService;
    private final S3Service s3Service;
    private final UserRepository userRepository;

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
            MultipartFile mediaFile,
            String mediaUrl
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

        String profileUrl = s3Service.uploadFile(profileImage);
        String resolvedMediaUrl = resolveMediaUrl(category, mediaFile, mediaUrl);
        String mediaType = resolveMediaType(category, mediaFile, resolvedMediaUrl);

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
        entry.setMediaUrl(resolvedMediaUrl);
        entry.setMediaType(mediaType);
        entry.setProcessingStatus("READY");
        entry.setThumbnailUrl(category == TalentCategory.ART ? null : buildYouTubeThumbnail(resolvedMediaUrl));

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
            MultipartFile mediaFile,
            String mediaUrl
    ) throws Exception {
        return participate(
                userId,
                name,
                dob,
                villageOrArea,
                phone,
                category,
                competitionId,
                isCompetition,
                profileImage,
                mediaFile,
                mediaUrl
        );
    }

    private String resolveMediaUrl(
            TalentCategory category,
            MultipartFile mediaFile,
            String mediaUrl
    ) throws Exception {

        if (category == TalentCategory.ART) {
            if (mediaFile != null && !mediaFile.isEmpty()) {
                return s3Service.uploadFile(mediaFile);
            }

            if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
                return mediaUrl.trim();
            }

            throw new Exception("ART entries require either an image file or an image URL.");
        }

        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            throw new Exception("This category requires a YouTube link.");
        }

        String trimmed = mediaUrl.trim();
        if (!isYouTubeUrl(trimmed)) {
            throw new Exception("Please provide a valid YouTube URL.");
        }

        return trimmed;
    }

    private String resolveMediaType(
            TalentCategory category,
            MultipartFile mediaFile,
            String resolvedMediaUrl
    ) {

        if (category == TalentCategory.ART) {
            if (mediaFile != null && !mediaFile.isEmpty()) {
                return getExt(mediaFile);
            }

            return getExtFromUrl(resolvedMediaUrl);
        }

        return "YOUTUBE";
    }

    private boolean isYouTubeUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = Optional.ofNullable(uri.getHost())
                    .orElse("")
                    .toLowerCase(Locale.ROOT);

            return host.equals("youtube.com")
                    || host.endsWith(".youtube.com")
                    || host.equals("youtu.be")
                    || host.endsWith(".youtu.be");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String buildYouTubeThumbnail(String url) {
        String videoId = extractYouTubeVideoId(url);
        if (videoId == null) {
            return null;
        }

        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }

    private String extractYouTubeVideoId(String url) {
        try {
            URI uri = URI.create(url);
            String host = Optional.ofNullable(uri.getHost())
                    .orElse("")
                    .toLowerCase(Locale.ROOT);

            if (host.endsWith("youtu.be")) {
                String path = uri.getPath();
                if (path == null || path.length() <= 1) {
                    return null;
                }
                return path.substring(1);
            }

            String query = uri.getQuery();
            if (query != null) {
                for (String part : query.split("&")) {
                    String[] pair = part.split("=", 2);
                    if (pair.length == 2 && "v".equals(pair[0])) {
                        return pair[1];
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return null;
    }

    private String getExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    private String getExtFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "LINK";
        }

        String cleanUrl = url.split("\\?")[0];
        int lastSlash = cleanUrl.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == cleanUrl.length() - 1) {
            return "LINK";
        }

        String name = cleanUrl.substring(lastSlash + 1);
        int dot = name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) {
            return "LINK";
        }

        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isS3Url(String url) {
        try {
            URI uri = URI.create(url);
            String host = Optional.ofNullable(uri.getHost())
                    .orElse("")
                    .toLowerCase(Locale.ROOT);

            return host.endsWith(".amazonaws.com") || host.equals("amazonaws.com");
        } catch (IllegalArgumentException ex) {
            return false;
        }
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

        if (entry.getMediaUrl() != null && isS3Url(entry.getMediaUrl()))
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
