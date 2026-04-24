package com.smartgaon.ai.smartgaon_api.enquiry;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestEnquiryService {

    private final GuestEnquiryRepository repository;

    public GuestEnquiry create(GuestEnquiry enquiry) {
        return repository.save(enquiry);
    }

    public List<GuestEnquiry> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public GuestEnquiry updateStatus(Long id, String status) {
        GuestEnquiry enquiry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found"));

        enquiry.setStatus(status);
        return repository.save(enquiry);
    }
}