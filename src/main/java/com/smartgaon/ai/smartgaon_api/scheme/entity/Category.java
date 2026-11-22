package com.smartgaon.ai.smartgaon_api.scheme.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // English (default) name
    @Column(nullable = false, unique = true)
    private String name;

    // Hindi, Marathi, ... names — same table the admin panel writes to
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_translations", joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "language", length = 10)
    @Column(name = "name", length = 255)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private Map<Language, String> nameTranslations = new HashMap<>();

    /** Name in the requested language, English if not translated. */
    public String localizedName(Language lang) {
        if (lang == null || lang == Language.DEFAULT) {
            return name;
        }
        String t = nameTranslations.get(lang);
        return (t == null || t.isBlank()) ? name : t;
    }
}
