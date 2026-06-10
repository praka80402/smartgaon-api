package com.smartgaon.ai.smartgaon_api.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sg_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SGEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "location")
    private String location;

    @Column(name = "venue")
    private String venue;

    @Column(name = "registration_link")
    private String registrationLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type")
    private EventSectionType sectionType;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "featured")
    private Boolean featured;
}