package com.smartgaon.ai.smartgaon_api.scheme.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

    // English (default) text
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    // Hindi, Marathi, ... text — same table the admin panel writes to
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scheme_translations", joinColumns = @JoinColumn(name = "scheme_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "language", length = 10)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private Map<Language, SchemeContent> translations = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "scheme_url")
    private String schemeUrl;

    @Column(name = "interest_count")
    @Builder.Default
    private Long interestCount = 0L;

}
