package com.smartgaon.ai.smartgaon_api.scheme.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "schemes",
        indexes = {
                @Index(name = "idx_scheme_category", columnList = "category_id"),
                @Index(name = "idx_scheme_type", columnList = "schemeType"),
                @Index(name = "idx_scheme_state", columnList = "state")
        }
)
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemeType schemeType;

    @Enumerated(EnumType.STRING)
    private State state;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "scheme_url")
    private String schemeUrl;
}
