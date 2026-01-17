package com.smartgaon.ai.smartgaon_api.banners.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;

    private String bannerTitle;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 2000)
    private String eventDetails;

    @ElementCollection
    @CollectionTable(
            name = "banner_images",
            joinColumns = @JoinColumn(name = "banner_id")
    )
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
}

