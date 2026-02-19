package com.smartgaon.ai.smartgaon_api.donation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "donation_reward")
public class DonationReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private DonationTransaction transaction;

    // uploaded signed certificate
    private String certificateUrl;

    private LocalDateTime assignedAt = LocalDateTime.now();
}
