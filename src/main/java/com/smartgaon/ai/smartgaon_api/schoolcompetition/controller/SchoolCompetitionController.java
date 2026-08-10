package com.smartgaon.ai.smartgaon_api.schoolcompetition.controller;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.service.SchoolCompetitionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school-competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolCompetitionController {

    private final SchoolCompetitionService competitionService;

    @GetMapping("/active")
    public ResponseEntity<List<SchoolCompetition>> getActiveCompetitions() {
        return ResponseEntity.ok(competitionService.getActiveCompetitions());
    }

    @GetMapping("/{competitionId}")
    public ResponseEntity<SchoolCompetition> getCompetitionDetails(@PathVariable String competitionId) {
        return ResponseEntity.ok(competitionService.getCompetitionById(competitionId));
    }

    @PostMapping("/create")
    public ResponseEntity<SchoolCompetition> createCompetition(@RequestBody SchoolCompetition competition) {
        return ResponseEntity.ok(competitionService.createCompetition(competition));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitCompetitionEntry(@RequestBody SubmissionRequest request) {
        try {
            SchoolCompetitionSubmission submission = SchoolCompetitionSubmission.builder()
                    .competitionId(request.getCompetitionId())
                    .studentName(request.getStudentName())
                    .schoolName(request.getSchoolName())
                    .classGrade(request.getClassGrade())
                    .rollNumber(request.getRollNumber())
                    .groupCategory(request.getGroupCategory())
                    .entryTitle(request.getEntryTitle())
                    .entryDescription(request.getEntryDescription())
                    .videoUrl(request.getVideoUrl())
                    .submittedBy(request.getSubmittedBy())
                    .build();

            SchoolCompetitionSubmission saved = competitionService.submitEntry(submission, request.getVerificationCode());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to submit entry: " + e.getMessage());
        }
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<List<SchoolCompetitionSubmission>> getMySubmissions(
            @RequestParam String schoolName,
            @RequestParam String rollNumber) {
        return ResponseEntity.ok(competitionService.getStudentSubmissions(schoolName, rollNumber));
    }

    @Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmissionRequest {
        private String competitionId;
        private String studentName;
        private String schoolName;
        private String classGrade;
        private String rollNumber;
        private String groupCategory;
        private String entryTitle;
        private String entryDescription;
        private String submissionType;
        private String videoUrl;
        private String verificationCode;
        private String submittedBy;
    }
}
