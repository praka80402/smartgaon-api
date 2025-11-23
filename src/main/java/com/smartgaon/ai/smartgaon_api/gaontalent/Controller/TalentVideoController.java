package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideo;
import com.smartgaon.ai.smartgaon_api.gaontalent.Service.TalentVideoService;

@RestController
@RequestMapping("/api/gaon-talent")
@RequiredArgsConstructor
public class TalentVideoController {

    private final TalentVideoService service;

    // -------------- UPLOAD -----------------
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam Long userId,
            @RequestParam boolean isReel
    ) {
        try {
            TalentVideo saved = service.uploadVideo(file, title, userId, isReel);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------- PAGINATED FEED --------------
    @GetMapping("/feed")
    public ResponseEntity<?> getPaginatedFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TalentVideo> videos = service.getPaginatedVideos(page, size);
        return ResponseEntity.ok(videos);
    }

    // -------------- LIKE ------------------------
    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(
            @PathVariable String id,
            @RequestParam Long userId
    ) {
        String result = service.likeVideo(id, userId);
        return ResponseEntity.ok(result);
    }


    // -------------- COMMENT ---------------------
    @PostMapping("/{id}/comment")
    public ResponseEntity<?> comment(@PathVariable String id) {
        service.commentVideo(id);
        return ResponseEntity.ok("Comment Added");
    }

    // -------------- SHARE ------------------------
    @PostMapping("/{id}/share")
    public ResponseEntity<?> share(@PathVariable String id) {
        service.shareVideo(id);
        return ResponseEntity.ok("Shared");
    }
}

