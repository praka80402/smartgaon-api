package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_reviews",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"job_id", "applicant_id"}
        ))
@Data
public class JobReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private Long applicantId;
    private Long employerId;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
}
