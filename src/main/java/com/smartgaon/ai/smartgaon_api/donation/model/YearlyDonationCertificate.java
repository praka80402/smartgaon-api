package com.smartgaon.ai.smartgaon_api.donation.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.smartgaon.ai.smartgaon_api.model.User;

@Entity
@Table(name = "yearly_donation_certificate",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","financial_year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyDonationCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* USER */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* FY (ex: 2025-26) */
    @Column(name = "financial_year", nullable = false)
    private String financialYear;

    /* STORED FILE PATH (same like image upload) */
    @Column(name = "file_path", length = 1000)
    private String filePath;

    /* uploaded date */
    private LocalDateTime uploadedAt = LocalDateTime.now();
}

