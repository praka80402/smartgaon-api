//package com.smartgaon.ai.smartgaon_api.donation.repository;
//
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//import com.smartgaon.ai.smartgaon_api.donation.model.DonationTransaction;
//
//public interface DonationTransactionRepository extends JpaRepository<DonationTransaction, Long> {
//
//    List<DonationTransaction> findByUserId(Long userId);
//
//}

package com.smartgaon.ai.smartgaon_api.donation.repository;

import com.smartgaon.ai.smartgaon_api.donation.model.DonationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationTransactionRepository extends JpaRepository<DonationTransaction, Long> {

    List<DonationTransaction> findByUserIdOrderByIdDesc(Long userId);
}
