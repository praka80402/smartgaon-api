package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.dto.VideoProcessingCallbackRequest;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoProcessingCallbackController {

    private final TalentEntryRepository entryRepo;

    @PostMapping("/processing-callback")
    public ResponseEntity<?> onVideoProcessed(
            @RequestBody VideoProcessingCallbackRequest request
    ) {

        TalentEntry entry = entryRepo.findById(request.getEntryId())
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setThumbnailUrl(request.getThumbnailUrl());
        entry.setLowQualityVideoUrl(request.getLowQualityVideoUrl());
        entry.setProcessingStatus("READY");

        entryRepo.save(entry);

        return ResponseEntity.ok("Video processing updated");
    }
}
