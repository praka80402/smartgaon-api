package com.smartgaon.ai.smartgaon_api.fertilizer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "crop_fertilizer_standards", indexes = {
        @Index(name = "idx_crop_key", columnList = "cropKey", unique = true),
        @Index(name = "idx_crop_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropFertilizerStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String cropKey;

    @Column(nullable = false, length = 120)
    private String nameEn;

    @Column(nullable = false, length = 120)
    private String nameHi;

    @Column(length = 120)
    private String nameMr;

    @Column(length = 20)
    private String icon;

    @Column(nullable = false, length = 40)
    private String category; // grains, pulses_oilseeds, cash_crops, vegetables, fruits, medicinal_spices

    @Column(nullable = false)
    private Double n; // Nitrogen requirement (kg/acre)

    @Column(nullable = false)
    private Double p; // Phosphorus requirement (kg/acre)

    @Column(nullable = false)
    private Double k; // Potassium requirement (kg/acre)

    private Double ureaPerAcre;

    private Double dapPerAcre;

    private Double mopPerAcre;

    private Double zincKgPerAcre;

    private String microNameEn;
    private String microNameHi;
    private String microNameMr;

    @Column(columnDefinition = "TEXT")
    private String stagesJson; // JSON array of stage objects

    @Column(length = 30)
    private String state; // "ALL" or specific state like "UP", "MH", "PB"

    private String source; // "ICAR / State Agricultural Universities"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
