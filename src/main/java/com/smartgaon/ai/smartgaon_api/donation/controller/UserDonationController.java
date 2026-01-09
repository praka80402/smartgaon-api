//package com.smartgaon.ai.smartgaon_api.donation.controller;
//
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import com.smartgaon.ai.smartgaon_api.donation.model.*;
//import com.smartgaon.ai.smartgaon_api.donation.service.UserDonationService;
//
//import java.util.List;
//
//
//
//@RestController
//@RequestMapping("/api/user")
//@RequiredArgsConstructor
//@CrossOrigin("*")
//public class UserDonationController {
//
//    private final UserDonationService service;
//
//    // VIEW PROJECTS
//    @GetMapping("/projects")
//    public List<DonationProject> projects() {
//        return service.getProjects();
//    }
//
//    // DONATE
//    @PostMapping("/donate")
//    public Donation donate(
//            @RequestParam Long userId,
//            @RequestParam Long projectId,
//            @RequestParam Double amount
//    ) {
//        return service.donate(userId, projectId, amount);
//    }
//
//    // DONATION HISTORY
//    @GetMapping("/donations/{userId}")
//    public List<Donation> history(@PathVariable Long userId) {
//        return service.history(userId);
//    }
//
//    // BADGES (PROFILE)
//    @GetMapping("/badges/{userId}")
//    public List<Badge> badges(@PathVariable Long userId) {
//        return service.badges(userId);
//    }
//}

package com.smartgaon.ai.smartgaon_api.donation.controller;

import com.smartgaon.ai.smartgaon_api.donation.dto.UserProjectDonationDTO;
import com.smartgaon.ai.smartgaon_api.donation.model.*;
import com.smartgaon.ai.smartgaon_api.donation.service.UserDonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserDonationController {

    private final UserDonationService service;

    // VIEW PROJECTS
    @GetMapping("/projects")
    public List<DonationProject> projects() {
        return service.getProjects();
    }
    
    @GetMapping("/projects/{id}")
    public DonationProject projectById(@PathVariable Long id) {
        return service.getProjectById(id);
    }

    // DONATE
    @PostMapping("/donate")
    public Donation donate(
            @RequestParam Long userId,
            @RequestParam Long projectId,
            @RequestParam Double amount
    ) {
        return service.donate(userId, projectId, amount);
    }

    // DONATION HISTORY (FIXED)
    @GetMapping("/donations/{userId}")
    public List<UserProjectDonationDTO> history(@PathVariable Long userId) {
        return service.history(userId);
    }

    // BADGES
    @GetMapping("/badges/{userId}")
    public List<Badge> badges(@PathVariable Long userId) {
        return service.badges(userId);
    }
}

