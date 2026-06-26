package com.smartgaon.ai.smartgaon_api.location.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pincodes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Pincode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pincode;

    @Column(name = "area_name")
    private String areaName;

    @Column(name = "district_id", nullable = false)
    private Long districtId;
}