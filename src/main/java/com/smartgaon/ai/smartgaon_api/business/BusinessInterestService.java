package com.smartgaon.ai.smartgaon_api.business;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessInterestService {

    private final BusinessInterestRepository repo;

    public void apply(ApplyBusinessInterestRequest req) {

        if (repo.existsByBusinessIdAndUserId(
                req.getBusinessId(), req.getUserId())) {
            throw new RuntimeException("Already applied");
        }

        BusinessInterest interest = new BusinessInterest();
        interest.setBusinessId(req.getBusinessId());
        interest.setUserId(req.getUserId());
        interest.setName(req.getName());
        interest.setPhone(req.getPhone());
        interest.setMessage(req.getMessage());

        repo.save(interest);
    }

    public List<BusinessInterestResponse> applicants(Long businessId) {

        return repo.findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .map(i -> new BusinessInterestResponse(
                        i.getId(),
                        i.getName(),
                        i.getPhone(),
                        i.getMessage(),
                        i.getCreatedAt()
                ))
                .toList();
    }
}
