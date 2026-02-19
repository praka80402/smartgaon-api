package com.smartgaon.ai.smartgaon_api.donation.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartgaon.ai.smartgaon_api.donation.enums.TransactionStatus;
import com.smartgaon.ai.smartgaon_api.model.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Month;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "donation_transaction")
public class DonationTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"donations"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private DonationCampaign campaign;

    private Double amount;

    // ⭐ UTR NUMBER
    private String utrNumber;

    private String paymentId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "donated_at", nullable = false, updatable = false)
    private LocalDateTime donatedAt;

    @Column(name = "financial_year", nullable = false, updatable = false)
    private String financialYear;

    @PrePersist
    public void setDonationTime() {

        // set current time when inserting
        this.donatedAt = LocalDateTime.now();

        int year = donatedAt.getYear();
        int month = donatedAt.getMonthValue();

        // financial year calculation (Apr–Mar)
        if (month >= 4)
            this.financialYear = year + "-" + String.valueOf(year + 1).substring(2);
        else
            this.financialYear = (year - 1) + "-" + String.valueOf(year).substring(2);
    }
}