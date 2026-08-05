package com.smartgaon.ai.smartgaon_api.schoolcompetition.repository;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetitionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolCompetitionSubmissionRepository extends JpaRepository<SchoolCompetitionSubmission, Long> {
    Optional<SchoolCompetitionSubmission> findBySubmissionId(String submissionId);
    List<SchoolCompetitionSubmission> findByCompetitionId(String competitionId);
    boolean existsByCompetitionIdAndSchoolNameAndRollNumber(String competitionId, String schoolName, String rollNumber);
    List<SchoolCompetitionSubmission> findBySchoolNameAndRollNumber(String schoolName, String rollNumber);
}
