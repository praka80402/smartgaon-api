package com.smartgaon.ai.smartgaon_api.schoolcompetition.service;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionRepository;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolCompetitionService {

    private final SchoolCompetitionRepository competitionRepository;
    private final SchoolCompetitionSubmissionRepository submissionRepository;

    public List<SchoolCompetition> getActiveCompetitions() {
        return competitionRepository.findByIsLiveTrue();
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
        if (!competition.getVerificationCode().equalsIgnoreCase(verificationCode.trim())) {
            throw new IllegalArgumentException("Invalid School Verification Code");
        }

        // 2. Enforce Duplicate Check (One entry per competition per student/roll no)
        boolean alreadySubmitted = submissionRepository.existsByCompetitionIdAndSchoolNameAndRollNumber(
                submission.getCompetitionId(),
                submission.getSchoolName(),
                submission.getRollNumber()
        );

        if (alreadySubmitted) {
            throw new IllegalStateException("Student has already submitted an entry for this competition!");
        }

        // 3. Set Submission ID & Default Status
        submission.setSubmissionId("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        submission.setStatus("SUBMITTED");

        return submissionRepository.save(submission);
    }

    public List<SchoolCompetitionSubmission> getStudentSubmissions(String schoolName, String rollNumber) {
        return submissionRepository.findBySchoolNameAndRollNumber(schoolName, rollNumber);
    }
}
