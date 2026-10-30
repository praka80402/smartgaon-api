package com.smartgaon.ai.smartgaon_api.mandi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mandi_rates", indexes = {
        @Index(name = "idx_mandi_trend", columnList = "commodity, district, arrivalDate"),
        @Index(name = "idx_mandi_arrival_date", columnList = "arrivalDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandiRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String market;

    @Column(nullable = false)
    private String commodity;

    private String variety;

    private Double minPrice;

    private Double maxPrice;

    private Double modalPrice;

    private Double msp;

    private String priceChange;

    private String arrivalDate;

    private String source; // "AGMARKNET_LIVE", "ENAM", "BENCHMARK"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
