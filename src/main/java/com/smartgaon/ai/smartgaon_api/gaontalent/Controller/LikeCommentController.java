package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.gaontalent.Service.LikeAndCommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gaon-talent")
@RequiredArgsConstructor
public class LikeCommentController {

    private final LikeAndCommentService service;

    /* ===== LIKE POST ===== */
    @PostMapping("/like/{entryId}")
    public ResponseEntity<?> like(
            @PathVariable Long entryId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(service.addLike(entryId, userId));
    }

    /* ===== ADD COMMENT ===== */
    @PostMapping("/comment/{entryId}")
    public ResponseEntity<?> comment(
            @PathVariable Long entryId,
            @RequestParam Long userId,
            @RequestParam String text
    ) {
        return ResponseEntity.ok(service.addComment(entryId, userId, text));
    }

    /* ===== GET COMMENTS ===== */
    @GetMapping("/comment/{entryId}")
    public ResponseEntity<?> getComments(@PathVariable Long entryId) {
        return ResponseEntity.ok(service.getComments(entryId));
    }

    /* ===== GET LIKE STATUS ===== */
    @GetMapping("/likes/{entryId}")
    public ResponseEntity<?> getLikes(
            @PathVariable Long entryId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(service.getLikes(entryId, userId));
    }
}
