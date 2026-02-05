package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;

//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoProcessingCallbackController {

    private final TalentEntryRepository entryRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/processing-callback")
    public ResponseEntity<String> onCallback(
            @org.springframework.web.bind.annotation.RequestBody String body) {

        try {
            JsonNode root = objectMapper.readTree(body);
            String type = root.path("Type").asText();

            System.out.println("RAW SNS BODY = " + body);

            // 1️⃣ Subscription confirmation
            if ("SubscriptionConfirmation".equals(type)) {
                String subscribeUrl = root.path("SubscribeURL").asText();
                restTemplate.getForObject(subscribeUrl, String.class);
                return ResponseEntity.ok("SNS subscription confirmed");
            }

            // 2️⃣ Notification
            if ("Notification".equals(type)) {

                JsonNode messageNode =
                        objectMapper.readTree(root.path("Message").asText());

                System.out.println("SNS MESSAGE = " + messageNode.toPrettyString());

                // 🔑 srcVideo is the only reliable mapper
                String srcVideo = messageNode.path("srcVideo").asText(null);
                if (srcVideo == null) {
                    return ResponseEntity.ok("Ignored: no srcVideo");
                }

                String fullS3Url =
                        "https://smartgaonvideosconverter-source71e471f1-ky0nypesuuxx.s3.ap-south-1.amazonaws.com/"
                                + srcVideo;

                TalentEntry entry = entryRepo.findByMediaUrl(fullS3Url)
                        .orElseThrow(() ->
                                new RuntimeException("Entry not found for srcVideo: " + srcVideo)
                        );

                // 🖼 Thumbnail (safe)
                JsonNode thumbs = messageNode.path("thumbNailsUrls");
                if (thumbs.isArray() && thumbs.size() > 0) {
                    entry.setThumbnailUrl(thumbs.get(0).asText());
                }

                // 🎥 Low quality video (HLS)
                String hlsUrl =
                        messageNode.path("egressEndpoints").path("HLS").asText(null);
                if (hlsUrl != null) {
                    entry.setLowQualityVideoUrl(hlsUrl);
                }

                entry.setProcessingStatus("READY");
                entryRepo.save(entry);

                return ResponseEntity.ok("Video processing updated");
            }

            return ResponseEntity.ok("Ignored");

        } catch (Exception e) {
            e.printStackTrace();
            // ❗ SNS expects 200, warna retry storm
            return ResponseEntity.ok("Handled");
        }
    }
}
