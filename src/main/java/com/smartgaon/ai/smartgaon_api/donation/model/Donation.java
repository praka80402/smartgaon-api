package com.smartgaon.ai.smartgaon_api.donation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private DonationProject project;

    private Double amount;
    private boolean verified;
}
