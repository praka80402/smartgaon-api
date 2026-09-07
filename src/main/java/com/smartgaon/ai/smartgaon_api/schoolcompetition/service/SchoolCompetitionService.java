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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    public List<SchoolCompetition> getAllCompetitionsForDisplay() {
        // Returns both LIVE and COMPLETED (non-deleted) competitions
        // Used by winner tab to show winners of completed competitions too
        return competitionRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .filter(c -> "LIVE".equalsIgnoreCase(c.getStatus()) || "COMPLETED".equalsIgnoreCase(c.getStatus()))
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
        validateSubmissionBeforeUpload(submission, verificationCode);
        requireText(submission.getVideoUrl(), "videoUrl");

        String videoUrl = submission.getVideoUrl();
        if (videoUrl != null && videoUrl.startsWith("data:")) {
            try {
                String s3Url = s3Service.uploadBase64File(videoUrl, "school-competitions");
                submission.setVideoUrl(s3Url);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to upload media file to S3: " + e.getMessage(), e);
            }
        }

        return saveSubmission(submission);
    }

    public SchoolCompetitionSubmission submitEntryWithFile(SchoolCompetitionSubmission submission, String verificationCode, MultipartFile file) {
        validateSubmissionBeforeUpload(submission, verificationCode);
        validateMediaFile(file);

        try {
            String s3Url = s3Service.uploadFileNew(file);
            submission.setVideoUrl(s3Url);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload media file to S3: " + e.getMessage(), e);
        }

        return saveSubmission(submission);
    }

    private void validateSubmissionBeforeUpload(SchoolCompetitionSubmission submission, String verificationCode) {
        if (submission == null) {
            throw new IllegalArgumentException("Submission data is required");
        }

        requireText(submission.getCompetitionId(), "competitionId");
        requireText(submission.getStudentName(), "studentName");
        requireText(submission.getSchoolName(), "schoolName");
        requireText(submission.getClassGrade(), "classGrade");
        requireText(submission.getRollNumber(), "rollNumber");
        requireText(submission.getGroupCategory(), "groupCategory");

        submission.setRollNumber(normalizeCommaSeparatedValue(submission.getRollNumber(), "rollNumber"));

        SchoolCompetition competition = getCompetitionById(submission.getCompetitionId());
        if (!Boolean.TRUE.equals(competition.getIsLive()) || Boolean.TRUE.equals(competition.getIsDeleted())) {
            throw new IllegalStateException("This competition is not accepting submissions");
        }

        String expectedCode = competition.getVerificationCode() == null ? "" : competition.getVerificationCode().trim();
        String providedCode = verificationCode == null ? "" : verificationCode.trim();
        if (!expectedCode.equalsIgnoreCase(providedCode)) {
            throw new IllegalArgumentException("Invalid School Verification Code");
        }

        boolean alreadySubmitted = submissionRepository.existsByCompetitionIdAndGroupCategoryAndClassGradeAndSchoolNameAndRollNumber(
                submission.getCompetitionId(), submission.getGroupCategory(), submission.getClassGrade(),
                submission.getSchoolName(), submission.getRollNumber());
        if (!alreadySubmitted) {
            alreadySubmitted = submissionRepository
                    .findByCompetitionIdAndGroupCategoryAndSchoolName(
                            submission.getCompetitionId(), submission.getGroupCategory(), submission.getSchoolName())
                    .stream()
                    .anyMatch(existing -> submission.getClassGrade().equals(existing.getClassGrade())
                            && submission.getRollNumber().equals(
                                    normalizeCommaSeparatedValue(existing.getRollNumber(), "rollNumber")));
        }
        if (alreadySubmitted) {
            throw new IllegalStateException("An entry has already been submitted for this student in this competition group");
        }
    }

    private void validateMediaFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A media file is required");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private String normalizeCommaSeparatedValue(String value, String fieldName) {
        String[] tokens = Arrays.stream(value.split(",", -1))
                .map(String::trim)
                .map(token -> token.toUpperCase(Locale.ROOT))
                .toArray(String[]::new);

        for (String token : tokens) {
            if (token.isEmpty()) {
                throw new IllegalArgumentException(fieldName + " contains an empty value");
            }
        }

        Arrays.sort(tokens);
        for (int index = 1; index < tokens.length; index++) {
            if (tokens[index - 1].equals(tokens[index])) {
                throw new IllegalArgumentException(fieldName + " cannot contain duplicate values");
            }
        }

        return String.join(",", tokens);
    }

    private SchoolCompetitionSubmission saveSubmission(SchoolCompetitionSubmission submission) {
        submission.setSubmissionId("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        submission.setStatus("SUBMITTED");
        return submissionRepository.save(submission);
    }

    @PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public List<SchoolCompetitionSubmission> getStudentSubmissions(String schoolName, String rollNumber) {
        requireText(schoolName, "schoolName");
        String normalizedRollNumber = normalizeCommaSeparatedValue(rollNumber, "rollNumber");
        return submissionRepository.findBySchoolName(schoolName).stream()
                .filter(submission -> normalizedRollNumber.equals(
                        normalizeCommaSeparatedValue(submission.getRollNumber(), "rollNumber")))
                .toList();
    }

    public List<SchoolCompetitionSubmission> getCompetitionWinners(String competitionId) {
        SchoolCompetition comp = getCompetitionById(competitionId);

        List<SchoolCompetitionSubmission> subs = submissionRepository.findByCompetitionId(competitionId).stream()
                .filter(s -> !"REJECTED".equalsIgnoreCase(s.getStatus()))
                .toList();

        java.util.Map<String, SchoolCompetitionSubmission> winnersMap = new java.util.LinkedHashMap<>();

        // 1. First include any submission with manual rank or WINNER status
        for (SchoolCompetitionSubmission s : subs) {
            if (s.getWinnerRank() != null && s.getWinnerRank() > 0) {
                winnersMap.put(s.getSubmissionId(), s);
            } else if ("WINNER".equalsIgnoreCase(s.getStatus()) || "WINNER_ANNOUNCED".equalsIgnoreCase(s.getStatus())) {
                s.setWinnerRank(1);
                winnersMap.put(s.getSubmissionId(), s);
            }
        }

        // 2. In AUTOMATIC mode (or if not MANUAL), also determine winners by total score per group
        boolean isAutomatic = comp == null || !"MANUAL".equalsIgnoreCase(comp.getWinnerAnnouncementMode());
        if (isAutomatic) {
            java.util.Map<String, List<SchoolCompetitionSubmission>> grouped = subs.stream()
                    .collect(java.util.stream.Collectors.groupingBy(s -> s.getGroupCategory() != null ? s.getGroupCategory() : "General"));

            for (java.util.Map.Entry<String, List<SchoolCompetitionSubmission>> entry : grouped.entrySet()) {
                List<SchoolCompetitionSubmission> groupSubs = entry.getValue();

                // Check ranks already used by manual winners in this group
                java.util.Set<Integer> usedRanks = new java.util.HashSet<>();
                for (SchoolCompetitionSubmission s : groupSubs) {
                    if (winnersMap.containsKey(s.getSubmissionId()) && winnersMap.get(s.getSubmissionId()).getWinnerRank() != null) {
                        usedRanks.add(winnersMap.get(s.getSubmissionId()).getWinnerRank());
                    }
                }

                // Sort non-rejected entries with scores descending
                List<SchoolCompetitionSubmission> sorted = groupSubs.stream()
                        .filter(s -> s.getTotalScore() != null && s.getTotalScore() > 0)
                        .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                        .toList();

                int currentRank = 1;
                for (SchoolCompetitionSubmission s : sorted) {
                    if (winnersMap.containsKey(s.getSubmissionId())) {
                        continue;
                    }
                    while (usedRanks.contains(currentRank)) {
                        currentRank++;
                    }
                    if (currentRank > 3) {
                        break;
                    }
                    s.setWinnerRank(currentRank);
                    usedRanks.add(currentRank);
                    winnersMap.put(s.getSubmissionId(), s);
                    currentRank++;
                }
            }
        }

        return winnersMap.values().stream()
                .sorted(java.util.Comparator.comparing((SchoolCompetitionSubmission s) -> s.getGroupCategory() != null ? s.getGroupCategory() : "")
                        .thenComparing(s -> s.getWinnerRank() != null ? s.getWinnerRank() : 999))
                .toList();
    }
}
