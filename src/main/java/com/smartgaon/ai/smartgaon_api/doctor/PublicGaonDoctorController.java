package com.smartgaon.ai.smartgaon_api.doctor;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PublicGaonDoctorController {

    private final PublicGaonDoctorService service;

    @Data
    public static class StartRequest {
        private String symptom;
    }

    @Data
    public static class NextRequest {
        private String category;
        private int questionNumber;
        private List<PublicGaonDoctorService.Answer> answers;
    }

    @PostMapping("/api/public/gaondoctor/start")
    public PublicGaonDoctorService.QuestionResponse start(@RequestBody StartRequest req) {
        return service.start(req.getSymptom());
    }

    @PostMapping("/api/public/gaondoctor/next")
    public Object next(@RequestBody NextRequest req) {
        return service.next(req.getCategory(), req.getQuestionNumber(), req.getAnswers());
    }
}
