package com.smartgaon.ai.smartgaon_api.donation.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "donation_projects",
        indexes = {
                @Index(name = "idx_project_state_pincode", columnList = "state,pincode"),
                @Index(name = "idx_project_all_states", columnList = "allStates")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    @Column(length = 1500)
    private String description;

    @Column(nullable = false)
    private Double requiredAmount;

    @Column(nullable = false)
    private Double raisedAmount = 0.0;

    @Enumerated(EnumType.STRING)
    private State state;

    private String pincode;

    private boolean allStates;

    // IMAGES
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "donation_project_images",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "image_url", length = 1000)
    private List<String> imageUrls = new ArrayList<>();

    // VIDEOS
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "donation_project_videos",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "video_url", length = 1000)
    private List<String> videoUrls = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "donation_project_gallery",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "gallery_image_url", length = 1000)
    private List<String> galleryImages = new ArrayList<>();


    // REMAINING AMOUNT (AUTO)
    @Transient
    public Double getRemainingAmount() {
        return requiredAmount - raisedAmount;
    }
}

