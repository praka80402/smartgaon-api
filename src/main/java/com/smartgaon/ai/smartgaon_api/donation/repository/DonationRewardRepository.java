package com.smartgaon.ai.smartgaon_api.donation.repository;

import com.smartgaon.ai.smartgaon_api.donation.model.DonationReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonationRewardRepository extends JpaRepository<DonationReward, Long> {

    Optional<DonationReward> findByTransactionId(Long transactionId);
}
