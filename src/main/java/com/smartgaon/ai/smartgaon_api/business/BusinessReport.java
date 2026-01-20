package com.smartgaon.ai.smartgaon_api.business;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "business_reports",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"business_id", "reporter_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessPost business;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String customReason;

    private LocalDateTime reportedAt = LocalDateTime.now();
}

