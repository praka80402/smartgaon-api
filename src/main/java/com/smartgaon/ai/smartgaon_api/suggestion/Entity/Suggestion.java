package com.smartgaon.ai.smartgaon_api.suggestion.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who submitted (optional: user id if present)
    private Long userId;

    // phone used to submit (helpful to contact)
    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String title;

    @Lob
    private String description;

    @Column(length = 6)
    private String pincode;

    @Column(length = 20)
    private String status; // e.g., "NEW", "REVIEW", "IN_PROGRESS", "COMPLETED"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = (status == null ? "NEW" : status);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

