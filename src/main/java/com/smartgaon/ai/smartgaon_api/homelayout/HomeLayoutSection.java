package com.smartgaon.ai.smartgaon_api.homelayout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "home_layout_section")
@Getter
@Setter
public class HomeLayoutSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_key")
    private String sectionKey;

    private String title;
    private Boolean visible;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_id")
    HomeLayout layout;
}
