//package com.smartgaon.ai.smartgaon_api.donation.service;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import com.smartgaon.ai.smartgaon_api.donation.model.*;
//import com.smartgaon.ai.smartgaon_api.donation.repository.*;
//
//import java.util.List;
//
//
//
//@Service
//@RequiredArgsConstructor
//public class UserDonationService {
//
//    private final DonationProjectRepository projectRepo;
//    private final DonationRepository donationRepo;
//    private final BadgeRepository badgeRepo;
//
//    // VIEW PROJECTS
//    public List<DonationProject> getProjects() {
//        return projectRepo.findAll();
//    }
//
//    // DONATE
////    public Donation donate(Long userId, Long projectId, Double amount) {
////        Donation donation = Donation.builder()
////                .userId(userId)
////                .projectId(projectId)
////                .amount(amount)
////                .verified(false)
////                .build();
////        return donationRepo.save(donation);
////    }
//    public Donation donate(Long userId, Long projectId, Double amount) {
//
//        DonationProject project = projectRepo.findById(projectId)
//                .orElseThrow(() -> new RuntimeException("Project not found"));
//
//        Donation donation = Donation.builder()
//                .userId(userId)
//                .project(project)
//                .amount(amount)
//                .verified(false)
//                .build();
//
//        return donationRepo.save(donation);
//    }
//
//    // DONATION HISTORY
//    public List<Donation> history(Long userId) {
//        return donationRepo.findByUserId(userId);
//    }
//
//    // USER BADGES
//    public List<Badge> badges(Long userId) {
//        return badgeRepo.findByUserId(userId);
//    }
//}
//

package com.smartgaon.ai.smartgaon_api.donation.service;

import com.smartgaon.ai.smartgaon_api.donation.dto.UserProjectDonationDTO;
import com.smartgaon.ai.smartgaon_api.donation.model.*;
import com.smartgaon.ai.smartgaon_api.donation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // DONATE (FIXED)
    public Donation donate(Long userId, Long projectId, Double amount) {

        DonationProject project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // ❌ Stop donation if target reached
        if (project.getRaisedAmount() >= project.getRequiredAmount()) {
            throw new RuntimeException("Donation goal already reached");
        }

        Double remaining = project.getRequiredAmount() - project.getRaisedAmount();

        // ❌ Stop over-donation
        if (amount > remaining) {
            throw new RuntimeException("Donation exceeds remaining amount");
        }

        Donation donation = Donation.builder()
                .userId(userId)
                .project(project)
                .amount(amount)
                .verified(false)
                .build();

        return donationRepo.save(donation);
    }
    public DonationProject getProjectById(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }


    // DONATION HISTORY (NO DUPLICATES + TOTAL)
    public List<UserProjectDonationDTO> history(Long userId) {

        List<Donation> donations = donationRepo.findByUserId(userId);

        Map<DonationProject, Double> grouped = donations.stream()
                .collect(Collectors.groupingBy(
                        Donation::getProject,
                        Collectors.summingDouble(Donation::getAmount)
                ));

        return grouped.entrySet()
                .stream()
                .map(e -> new UserProjectDonationDTO(
                        e.getKey(),
                        e.getValue()
                ))
                .toList();
    }

    // USER BADGES
    public List<Badge> badges(Long userId) {
        return badgeRepo.findByUserId(userId);
    }
}

