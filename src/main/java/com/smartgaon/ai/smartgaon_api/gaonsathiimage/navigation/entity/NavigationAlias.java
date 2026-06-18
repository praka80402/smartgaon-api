package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "navigation_aliases",
        indexes = {
                @Index(
                        name = "idx_alias_text",
                        columnList = "alias_text"
                ),
                @Index(
                        name = "idx_module_code",
                        columnList = "module_code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "module_code",
            nullable = false,
            length = 100
    )
    private String moduleCode;

    @Column(
            name = "alias_text",
            nullable = false,
            length = 255
    )
    private String aliasText;

    @Column(length = 20)
    private String language;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}