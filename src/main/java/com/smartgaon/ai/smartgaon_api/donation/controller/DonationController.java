
//
//package com.smartgaon.ai.smartgaon_api.donation.controller;
//
//import com.smartgaon.ai.smartgaon_api.donation.dto.CampaignResponse;
//import com.smartgaon.ai.smartgaon_api.donation.dto.DonateRequest;
//import com.smartgaon.ai.smartgaon_api.donation.dto.DonateResponse;
//import com.smartgaon.ai.smartgaon_api.donation.model.DonationTransaction;
//import com.smartgaon.ai.smartgaon_api.donation.service.DonationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/donations")
//@RequiredArgsConstructor
//@CrossOrigin("*")
//public class DonationController {
//
//    private final DonationService donationService;
//
//    /* =====================================================
//       CAMPAIGNS
//       ===================================================== */
//
////    // All campaigns
////    @GetMapping("/campaigns")
////    public List<CampaignResponse> getAllCampaigns(Authentication auth) {
////        Long userId = Long.parseLong(auth.getName());
////        return donationService.getAllCampaigns(userId);
////    }
//
//    // Only PROJECT (state filtered)
//    @GetMapping("/projects")
//    public List<CampaignResponse> getProjects() {
//        return donationService.getProjects();   // no auth, no state
//    }
//
//
//    // Only PROGRAM (visible to all)
//    @GetMapping("/programs")
//    public List<CampaignResponse> getPrograms() {
//        return donationService.getPrograms();
//    }
//
//    /* =====================================================
//       DONATE
//       ===================================================== */
//
//    @PostMapping("/donate")
//    public DonateResponse donate(@RequestBody DonateRequest request,
//                                 Authentication auth) {
//
//        Long userId = Long.parseLong(auth.getName());
//        return donationService.donate(userId, request);
//    }
//
//    /* =====================================================
//       USER DONATION HISTORY
//       ===================================================== */
//
//    @GetMapping("/my")
//    public List<DonationTransaction> myDonations(Authentication auth) {
//        Long userId = Long.parseLong(auth.getName());
//        return donationService.getUserDonations(userId);
//    }
//
//    /* =====================================================
//       RECEIPT DOWNLOAD (after admin verify)
//       ===================================================== */
//
//    @GetMapping("/receipt/{transactionId}")
//    public String getReceipt(@PathVariable Long transactionId) {
//        return donationService.getReceipt(transactionId);
//    }
//
//    /* =====================================================
//       YEARLY TAX CERTIFICATE
//       ===================================================== */
//
//    @GetMapping("/yearly/{financialYear}")
//    public String getYearlyCertificate(Authentication auth,
//                                       @PathVariable String financialYear) {
//
//        Long userId = Long.parseLong(auth.getName());
//        return donationService.getYearlyCertificate(userId, financialYear);
//    }
//}
//

package com.smartgaon.ai.smartgaon_api.donation.controller;

import com.smartgaon.ai.smartgaon_api.donation.dto.CampaignResponse;
import com.smartgaon.ai.smartgaon_api.donation.dto.DonateRequest;
import com.smartgaon.ai.smartgaon_api.donation.dto.DonateResponse;
import com.smartgaon.ai.smartgaon_api.donation.model.DonationTransaction;
import com.smartgaon.ai.smartgaon_api.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DonationController {

    private final DonationService donationService;

    /* =====================================================
       CAMPAIGNS
       ===================================================== */

    // PROJECTS
    @GetMapping("/projects")
    public List<CampaignResponse> getProjects() {
        return donationService.getProjects();
    }

    // PROGRAMS
    @GetMapping("/programs")
    public List<CampaignResponse> getPrograms() {
        return donationService.getPrograms();
    }

    /* =====================================================
       DONATE
       ===================================================== */

    @PostMapping("/donate")
    public DonateResponse donate(
            @RequestParam Long userId,
            @RequestBody DonateRequest request
    ) {
        return donationService.donate(userId, request);
    }

    /* =====================================================
       USER DONATIONS HISTORY
       ===================================================== */

    @GetMapping("/my/{userId}")
    public List<DonationTransaction> myDonations(@PathVariable Long userId) {
        return donationService.getUserDonations(userId);
    }

    /* =====================================================
       RECEIPT (after admin verification)
       ===================================================== */

    @GetMapping("/receipt/{transactionId}")
    public String getReceipt(@PathVariable Long transactionId) {
        return donationService.getReceipt(transactionId);
    }

    /* =====================================================
       YEARLY CERTIFICATE
       ===================================================== */

    @GetMapping("/yearly/{userId}/{financialYear}")
    public String getYearlyCertificate(
            @PathVariable Long userId,
            @PathVariable String financialYear
    ) {
        return donationService.getYearlyCertificate(userId, financialYear);
    }
}

