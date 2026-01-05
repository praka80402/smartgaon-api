package com.smartgaon.ai.smartgaon_api.donation.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.donation.model.*;
import com.smartgaon.ai.smartgaon_api.donation.repository.*;

import java.util.List;



@Service
@RequiredArgsConstructor
public class UserDonationService {

    private final DonationProjectRepository projectRepo;
    private final DonationRepository donationRepo;
    private final BadgeRepository badgeRepo;

    // VIEW PROJECTS
    public List<DonationProject> getProjects() {
        return projectRepo.findAll();
    }

    // DONATE
//    public Donation donate(Long userId, Long projectId, Double amount) {
//        Donation donation = Donation.builder()
//                .userId(userId)
//                .projectId(projectId)
//                .amount(amount)
//                .verified(false)
//                .build();
//        return donationRepo.save(donation);
//    }
    public Donation donate(Long userId, Long projectId, Double amount) {

        DonationProject project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Donation donation = Donation.builder()
                .userId(userId)
                .project(project)
                .amount(amount)
                .verified(false)
                .build();

        return donationRepo.save(donation);
    }

    // DONATION HISTORY
    public List<Donation> history(Long userId) {
        return donationRepo.findByUserId(userId);
    }

    // USER BADGES
    public List<Badge> badges(Long userId) {
        return badgeRepo.findByUserId(userId);
    }
}

