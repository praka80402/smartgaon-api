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

    /* ---------------- LIKE ---------------- */
    @PostMapping("/like/{entryId}")
    public ResponseEntity<?> like(
            @PathVariable Long entryId,
            @RequestParam Long userId   // <-- frontend must send this
    ) {
        return ResponseEntity.ok(service.like(entryId, userId));
    }

    /* ---------------- COMMENT ---------------- */
    @PostMapping("/comment/{entryId}")
    public ResponseEntity<?> comment(
            @PathVariable Long entryId,
            @RequestParam Long userId,
            @RequestParam String text
    ) {
        return ResponseEntity.ok(service.addComment(entryId, userId, text));
    }
}
