package com.smartgaon.ai.smartgaon_api.gaonsaathi.controller;

import com.smartgaon.ai.smartgaon_api.gaonsaathi.dto.GaonSaathiChatRequest;
import com.smartgaon.ai.smartgaon_api.gaonsaathi.dto.GaonSaathiChatResponse;
import com.smartgaon.ai.smartgaon_api.gaonsaathi.service.GaonSaathiRagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class GaonSaathiRagController {

    @Autowired
    private GaonSaathiRagService gaonSaathiRagService;

    @PostMapping("/gaonsaathi-rag")
    public ResponseEntity<GaonSaathiChatResponse> handleGaonSaathiChat(@RequestBody GaonSaathiChatRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            GaonSaathiChatResponse errResp = new GaonSaathiChatResponse(
                    "Kshama kijiye, aapne koi sawal nahi pucha hai.",
                    null,
                    null,
                    null
            );
            return ResponseEntity.badRequest().body(errResp);
        }

        GaonSaathiChatResponse response = gaonSaathiRagService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
