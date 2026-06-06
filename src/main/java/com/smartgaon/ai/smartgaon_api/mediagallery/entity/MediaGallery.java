package com.smartgaon.ai.smartgaon_api.mediagallery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String category;

    private String mediaType;

    private String sourceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String mediaUrl;

    private String thumbnailUrl;

    private Boolean active;
}