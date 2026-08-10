package com.smartgaon.ai.smartgaon_api.schoolcompetition.service;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionRepository;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionSubmissionRepository;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolCompetitionService {

    private final SchoolCompetitionRepository competitionRepository;
    private final SchoolCompetitionSubmissionRepository submissionRepository;
    private final S3Service s3Service;

    public List<SchoolCompetition> getActiveCompetitions() {
        return competitionRepository.findByIsLiveTrueAndIsDeletedFalse();
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

    public List<SchoolCompetitionSubmission> getStudentSubmissions(String schoolName, String rollNumber) {
        return submissionRepository.findBySchoolNameAndRollNumber(schoolName, rollNumber);
    }
}
