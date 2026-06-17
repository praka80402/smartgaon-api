package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "navigation_routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = "module_code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "module_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String moduleCode;

    @Column(
            name = "module_name",
            nullable = false,
            length = 255
    )
    private String moduleName;

    @Column(
            name = "route_path",
            nullable = false,
            length = 255
    )
    private String routePath;

    @Column(length = 100)
    private String icon;

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