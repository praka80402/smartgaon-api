package com.smartgaon.ai.smartgaon_api.successstory.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "success_stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String userName;

    @Column(length = 3000, nullable = false)
    private String story;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Column(nullable = false, length = 6)
    private String pincode;

    @Column(nullable = false)
    private String profileImageUrl;

    private boolean published;

    private LocalDateTime createdAt;
}

