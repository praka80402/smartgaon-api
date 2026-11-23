package com.smartgaon.ai.smartgaon_api.fertilizer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fertilizer_prices", indexes = {
        @Index(name = "idx_fertilizer_item_key", columnList = "itemKey", unique = true),
        @Index(name = "idx_fertilizer_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FertilizerPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String itemKey;

    @Column(nullable = false, length = 120)
    private String nameEn;

    @Column(nullable = false, length = 120)
    private String nameHi;

    @Column(length = 120)
    private String nameMr;

    @Column(nullable = false, length = 40)
    private String category; // CHEMICAL, ORGANIC, NANO, MICRONUTRIENT

    @Column(nullable = false, length = 40)
    private String unit; // BAG_45KG, BAG_50KG, KG, TROLLEY, BOTTLE_500ML, KIT, LITRE

    @Column(nullable = false)
    private Double price; // Current subsidized / effective farmer retail price

    private Double mrp; // Unsubsidized MRP / market benchmark

    private Double subsidyAmount; // Govt of India NBS subsidy amount per bag/unit

    private String brand; // IFFCO, KRIBHCO, IPL, NFL, National

    private String source; // "Govt. of India / IFFCO eBazar"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
