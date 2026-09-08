package com.smartgaon.ai.smartgaon_api.schoolcompetition.controller;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.PrizeDistributionVideo;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.PrizeDistributionVideoRepository;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.service.SchoolCompetitionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/school-competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolCompetitionController {

    private final SchoolCompetitionService competitionService;
    private final PrizeDistributionVideoRepository prizeVideoRepository;

    @GetMapping("/active")
    public ResponseEntity<List<SchoolCompetition>> getActiveCompetitions() {
        return ResponseEntity.ok(competitionService.getActiveCompetitions());
    }

    @GetMapping("/all")
    public ResponseEntity<List<SchoolCompetition>> getAllCompetitions() {
        // Returns LIVE + COMPLETED competitions for the winners display tab
        return ResponseEntity.ok(competitionService.getAllCompetitionsForDisplay());
    }

    @GetMapping("/debug-competitions")
    public ResponseEntity<List<SchoolCompetition>> debugCompetitions() {
        return ResponseEntity.ok(competitionService.debugAllCompetitions());
    }

    @GetMapping("/debug-submissions")
    public ResponseEntity<List<SchoolCompetitionSubmission>> debugSubmissions() {
        return ResponseEntity.ok(competitionService.debugAllSubmissions());
    }

    @GetMapping("/ceremony-videos")
    public ResponseEntity<List<PrizeDistributionVideo>> getCeremonyVideos() {
        return ResponseEntity.ok(prizeVideoRepository.findAll().stream()
                .filter(v -> v.getVideoUrl() != null && !v.getVideoUrl().isBlank())
                .filter(v -> !Boolean.TRUE.equals(v.getIsPastCompetition()))
                .filter(v -> v.getWinnerRank() == null || v.getWinnerRank() <= 0)
                .filter(v -> v.getStudentName() == null || v.getStudentName().isBlank())
                .toList());
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

    @PostMapping(value = "/submit-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitCompetitionEntryWithFile(
            @RequestPart("data") SubmissionRequest request,
            @RequestPart("file") MultipartFile file) {
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
                    .submittedBy(request.getSubmittedBy())
                    .build();

            SchoolCompetitionSubmission saved = competitionService.submitEntryWithFile(submission, request.getVerificationCode(), file);
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

    @GetMapping("/{competitionId}/winners")
    public ResponseEntity<List<SchoolCompetitionSubmission>> getWinners(@PathVariable String competitionId) {
        return ResponseEntity.ok(competitionService.getCompetitionWinners(competitionId));
    }

    @GetMapping("/prize-videos")
    public ResponseEntity<List<PrizeDistributionVideo>> getPrizeVideos(
            @RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(prizeVideoRepository.findByCategoryIgnoreCase(category.trim()));
        }
        return ResponseEntity.ok(prizeVideoRepository.findAll());
    }

    @GetMapping("/ceremony-videos")
    public ResponseEntity<List<PrizeDistributionVideo>> getCeremonyVideos() {
        return ResponseEntity.ok(prizeVideoRepository.findByIsPastCompetitionFalseOrIsPastCompetitionIsNull());
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