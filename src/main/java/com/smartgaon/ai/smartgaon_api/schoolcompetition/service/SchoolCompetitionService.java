package com.smartgaon.ai.smartgaon_api.schoolcompetition.service;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionRepository;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionSubmissionRepository;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolCompetitionService {

    private final SchoolCompetitionRepository competitionRepository;
    private final SchoolCompetitionSubmissionRepository submissionRepository;
    private final S3Service s3Service;

    public List<SchoolCompetition> getActiveCompetitions() {
        return competitionRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsLive()) && !Boolean.TRUE.equals(c.getIsDeleted()))
                .toList();
    }

    public List<SchoolCompetition> debugAllCompetitions() {
        return competitionRepository.findAll();
    }

    public List<SchoolCompetitionSubmission> debugAllSubmissions() {
        return submissionRepository.findAll();
    }

    public SchoolCompetition getCompetitionById(String competitionId) {
        return competitionRepository.findByCompetitionId(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found with ID: " + competitionId));
    }

    public SchoolCompetition createCompetition(SchoolCompetition competition) {
        if (competition.getCompetitionId() == null || competition.getCompetitionId().isBlank()) {
            competition.setCompetitionId("COMP-2026-AUG-001");
        }
        if (competition.getVerificationCode() == null || competition.getVerificationCode().isBlank()) {
            competition.setVerificationCode("SG-SCHOOL-2026");
        }
        return competitionRepository.save(competition);
    }

    public SchoolCompetitionSubmission submitEntry(SchoolCompetitionSubmission submission, String verificationCode) {
        SchoolCompetition competition = getCompetitionById(submission.getCompetitionId());

        // 1. Validate Verification Code (Single code for all schools)
        String expectedCode = competition.getVerificationCode() != null ? competition.getVerificationCode().trim() : "";
        String providedCode = verificationCode != null ? verificationCode.trim() : "";
        if (!expectedCode.equalsIgnoreCase(providedCode)) {
            throw new IllegalArgumentException("Invalid School Verification Code: '" + providedCode + "' does not match competition code.");
        }

        // 2. Enforce Duplicate Check (One entry per competition per group, class, school and student roll no)
        boolean alreadySubmitted = submissionRepository.existsByCompetitionIdAndGroupCategoryAndClassGradeAndSchoolNameAndRollNumber(
                submission.getCompetitionId(),
                submission.getGroupCategory(),
                submission.getClassGrade(),
                submission.getSchoolName(),
                submission.getRollNumber()
        );

        if (alreadySubmitted) {
            throw new IllegalStateException("An entry has already been submitted for this Student (Roll: " + submission.getRollNumber() + ", Class: " + submission.getClassGrade() + ", Group: " + submission.getGroupCategory() + ") under " + submission.getSchoolName() + "!");
        }

        // 3. Upload base64 file content (videos, images, or documents) to S3 if applicable
        String videoUrl = submission.getVideoUrl();
        if (videoUrl != null && videoUrl.startsWith("data:")) {
            try {
                String s3Url = s3Service.uploadBase64File(videoUrl, "school-competitions");
                submission.setVideoUrl(s3Url);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to upload media file to S3: " + e.getMessage(), e);
            }
        }

        // 4. Set Submission ID & Default Status
        submission.setSubmissionId("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        submission.setStatus("SUBMITTED");

        return submissionRepository.save(submission);
    }

    public SchoolCompetitionSubmission submitEntryWithFile(SchoolCompetitionSubmission submission, String verificationCode, MultipartFile file) {
        SchoolCompetition competition = getCompetitionById(submission.getCompetitionId());

        // 1. Validate Verification Code (Single code for all schools)
        String expectedCode = competition.getVerificationCode() != null ? competition.getVerificationCode().trim() : "";
        String providedCode = verificationCode != null ? verificationCode.trim() : "";
        if (!expectedCode.equalsIgnoreCase(providedCode)) {
            throw new IllegalArgumentException("Invalid School Verification Code: '" + providedCode + "' does not match competition code.");
        }

        // 2. Enforce Duplicate Check (One entry per competition per group, class, school and student roll no)
        boolean alreadySubmitted = submissionRepository.existsByCompetitionIdAndGroupCategoryAndClassGradeAndSchoolNameAndRollNumber(
                submission.getCompetitionId(),
                submission.getGroupCategory(),
                submission.getClassGrade(),
                submission.getSchoolName(),
                submission.getRollNumber()
        );

        if (alreadySubmitted) {
            throw new IllegalStateException("An entry has already been submitted for this Student (Roll: " + submission.getRollNumber() + ", Class: " + submission.getClassGrade() + ", Group: " + submission.getGroupCategory() + ") under " + submission.getSchoolName() + "!");
        }

        // 3. Upload the raw file directly to S3 (no base64 involved)
        if (file != null && !file.isEmpty()) {
            try {
                String s3Url = s3Service.uploadFile(file);
                submission.setVideoUrl(s3Url);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to upload media file to S3: " + e.getMessage(), e);
            }
        }

        // 4. Set Submission ID & Default Status
        submission.setSubmissionId("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        submission.setStatus("SUBMITTED");

        return submissionRepository.save(submission);
    }

    @PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public List<SchoolCompetitionSubmission> getStudentSubmissions(String schoolName, String rollNumber) {
        return submissionRepository.findBySchoolNameAndRollNumber(schoolName, rollNumber);
    }

    public List<SchoolCompetitionSubmission> getCompetitionWinners(String competitionId) {
        SchoolCompetition comp = getCompetitionById(competitionId);
        
        if ("MANUAL".equalsIgnoreCase(comp.getWinnerAnnouncementMode())) {
            return submissionRepository.findByCompetitionId(competitionId).stream()
                    .filter(s -> s.getWinnerRank() != null && s.getWinnerRank() > 0)
                    .sorted(java.util.Comparator.comparing(SchoolCompetitionSubmission::getWinnerRank))
                    .toList();
        } else {
            // AUTOMATIC mode:
            // 1. Get total judges count in the system
            long totalJudges = 3; // default fallback
            try {
                Number countNum = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM admins WHERE role = 'JUDGE'"
                ).getSingleResult();
                if (countNum != null) {
                    totalJudges = countNum.longValue();
                }
            } catch (Exception e) {
                // Ignore and use default fallback 3
            }
            if (totalJudges <= 0) {
                totalJudges = 3;
            }

            // 2. Fetch all active submissions for this competition
            List<SchoolCompetitionSubmission> subs = submissionRepository.findByCompetitionId(competitionId).stream()
                    .filter(s -> !"REJECTED".equalsIgnoreCase(s.getStatus()))
                    .toList();

            // Group by groupCategory
            java.util.Map<String, List<SchoolCompetitionSubmission>> grouped = subs.stream()
                    .collect(java.util.stream.Collectors.groupingBy(SchoolCompetitionSubmission::getGroupCategory));

            List<SchoolCompetitionSubmission> winners = new java.util.ArrayList<>();

            for (java.util.Map.Entry<String, List<SchoolCompetitionSubmission>> entry : grouped.entrySet()) {
                List<SchoolCompetitionSubmission> groupSubs = entry.getValue();
                
                // For each group, check if ALL entries in that group have been evaluated by ALL judges
                boolean allEvaluated = true;
                for (SchoolCompetitionSubmission s : groupSubs) {
                    long evalsCount = 0;
                    try {
                        Number countNum = (Number) entityManager.createNativeQuery(
                                "SELECT COUNT(*) FROM judge_evaluations WHERE submission_id = :subId"
                        ).setParameter("subId", s.getSubmissionId()).getSingleResult();
                        if (countNum != null) {
                            evalsCount = countNum.longValue();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    if (evalsCount < totalJudges) {
                        allEvaluated = false;
                        break;
                    }
                }

                if (allEvaluated) {
                    // Filter those with valid scores, sort descending, and take top 3
                    List<SchoolCompetitionSubmission> sorted = groupSubs.stream()
                            .filter(s -> s.getTotalScore() != null)
                            .sorted((a, b) -> b.getTotalScore() - a.getTotalScore())
                            .toList();

                    for (int i = 0; i < Math.min(sorted.size(), 3); i++) {
                        SchoolCompetitionSubmission w = sorted.get(i);
                        w.setWinnerRank(i + 1); // Set rank (1, 2, or 3)
                        winners.add(w);
                    }
                }
            }

            return winners.stream()
                    .sorted(java.util.Comparator.comparing(SchoolCompetitionSubmission::getGroupCategory)
                            .thenComparing(SchoolCompetitionSubmission::getWinnerRank))
                    .toList();
        }
    }
}