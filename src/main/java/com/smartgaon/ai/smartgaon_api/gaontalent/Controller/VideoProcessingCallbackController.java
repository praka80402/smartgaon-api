package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.dto.VideoProcessingCallbackRequest;

//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoProcessingCallbackController {

    private final TalentEntryRepository entryRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/processing-callback")
    public ResponseEntity<?> onCallback(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload
    ) {

        String type = (String) payload.get("Type");

        // ✅ 1️⃣ SNS subscription confirmation
        if ("SubscriptionConfirmation".equals(type)) {

            String subscribeUrl = (String) payload.get("SubscribeURL");
            restTemplate.getForObject(subscribeUrl, String.class);

            return ResponseEntity.ok("SNS subscription confirmed");
        }

        // ✅ 2️⃣ Actual notification (video processed)
        if ("Notification".equals(type)) {

            String message = (String) payload.get("Message");

            // Message is STRING → parse JSON
            ObjectMapper mapper = new ObjectMapper();
            VideoProcessingCallbackRequest request;

            try {
                request = mapper.readValue(message, VideoProcessingCallbackRequest.class);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid message format");
            }

            TalentEntry entry = entryRepo.findById(request.getEntryId())
                    .orElseThrow(() -> new RuntimeException("Entry not found"));

            entry.setThumbnailUrl(request.getThumbnailUrl());
            entry.setLowQualityVideoUrl(request.getLowQualityVideoUrl());
            entry.setProcessingStatus("READY");

            entryRepo.save(entry);

            return ResponseEntity.ok("Video processing updated");
        }

        return ResponseEntity.ok("Ignored");
    }
}
