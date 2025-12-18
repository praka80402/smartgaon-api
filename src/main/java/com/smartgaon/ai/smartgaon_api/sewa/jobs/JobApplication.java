package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "applicant_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    private String status; // PENDING / ACCEPTED / REJECTED

    private LocalDateTime appliedAt;
}
