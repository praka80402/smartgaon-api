package com.smartgaon.ai.smartgaon_api.sewa.jobs;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "job_reports",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "reporter_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String customReason;

    private LocalDateTime reportedAt = LocalDateTime.now();
}
