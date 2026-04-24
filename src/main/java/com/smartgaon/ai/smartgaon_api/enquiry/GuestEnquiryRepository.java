package com.smartgaon.ai.smartgaon_api.enquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestEnquiryRepository extends JpaRepository<GuestEnquiry, Long> {
}