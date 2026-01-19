package com.smartgaon.ai.smartgaon_api.GaonConnectForum.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
    name = "forum_post_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "reported_by"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private ForumPost post;

    @Column(name = "reported_by", nullable = false)
    private Long reportedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String customReason; 

    private Instant reportedAt = Instant.now();
}

