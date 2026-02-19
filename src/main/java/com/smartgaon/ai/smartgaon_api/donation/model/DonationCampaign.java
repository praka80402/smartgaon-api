package com.smartgaon.ai.smartgaon_api.donation.model;


import java.util.ArrayList;
import java.util.List;

import com.smartgaon.ai.smartgaon_api.donation.enums.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "donation_campaign")
public class DonationCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    private State state;

    // NEW LOCATION FIELDS
    private String pincode;
    private String district;
    private String village;

    private Double targetAmount;
    private Double raisedAmount = 0.0;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    private String imageUrl;
    
    /* ================= GALLERY ================= */

    @ElementCollection
    @CollectionTable(name = "campaign_gallery",
            joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "image_url")
    private List<String> galleryImages = new ArrayList<>();
}
