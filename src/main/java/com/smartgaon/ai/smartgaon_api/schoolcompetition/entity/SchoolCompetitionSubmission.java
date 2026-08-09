package com.smartgaon.ai.smartgaon_api.schoolcompetition.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_competition_submissions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_comp_student_new", columnNames = {"competition_id", "group_category", "class_grade", "school_name", "roll_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolCompetitionSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false, unique = true, length = 64)
    private String submissionId;

    @Column(name = "competition_id", nullable = false, length = 50)
    private String competitionId;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "school_name", nullable = false, length = 255)
    private String schoolName;

    @Column(name = "class_grade", nullable = false, length = 50)
    private String classGrade;

    @Column(name = "roll_number", nullable = false, length = 50)
    private String rollNumber;

    @Column(name = "group_category", nullable = false, length = 100)
    private String groupCategory;

    @Column(name = "entry_title", length = 255)
    private String entryTitle;

    @Column(name = "entry_description", columnDefinition = "TEXT")
    private String entryDescription;

    @Column(name = "video_url", nullable = false, columnDefinition = "LONGTEXT")
    private String videoUrl;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "SUBMITTED"; // SUBMITTED, UNDER_REVIEW, WINNER_ANNOUNCED

    @Column(name = "submitted_by", length = 150)
    private String submittedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
