package com.smartgaon.ai.smartgaon_api.scheme.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/** Scheme text in one language. Rows of the scheme_translations table (filled by the admin panel). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchemeContent {

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "eligibility", columnDefinition = "TEXT")
    private String eligibility;

    @JsonIgnore
    public boolean isEmpty() {
        return isBlank(title) && isBlank(detail) && isBlank(benefits) && isBlank(eligibility);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
