package com.smartgaon.ai.smartgaon_api.donation.repository;

import com.smartgaon.ai.smartgaon_api.donation.model.YearlyDonationCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YearlyDonationCertificateRepository
        extends JpaRepository<YearlyDonationCertificate, Long> {

    // get certificate for specific user + financial year
    Optional<YearlyDonationCertificate> findByUserIdAndFinancialYear(Long userId, String financialYear);
}
