package com.smartgaon.ai.smartgaon_api.delete;

import com.smartgaon.ai.smartgaon_api.delete.AccountDeleteRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountDeleteRequestRepository extends JpaRepository<AccountDeleteRequest, Long> {

    Optional<AccountDeleteRequest> findTopByPhoneOrderByCreatedAtDesc(String phone);

    boolean existsByPhoneAndStatus(String phone, Status status);

    List<AccountDeleteRequest> findAllByOrderByCreatedAtDesc();

    List<AccountDeleteRequest> findByStatusOrderByCreatedAtDesc(Status status);
}