package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "community_posts",
        indexes = {
                @Index(name = "idx_topic", columnList = "topic"),
                @Index(name = "idx_tag_village", columnList = "tagVillage"),
                @Index(name = "idx_date_posted", columnList = "date_posted"),
                @Index(name = "idx_created_at", columnList = "createdAt"),
                @Index(name = "idx_posted_by", columnList = "postedBy")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(length = 1000)   // Cloudinary URL stays within 1000 chars
    private String pictureUrl;

    @Column(name = "date_posted")
    private LocalDateTime datePosted;

    private String postedBy;

    private String tagVillage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        datePosted = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
