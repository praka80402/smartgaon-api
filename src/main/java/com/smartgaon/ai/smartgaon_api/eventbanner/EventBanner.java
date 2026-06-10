package com.smartgaon.ai.smartgaon_api.eventbanner;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_banner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private BannerSectionType sectionType;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}