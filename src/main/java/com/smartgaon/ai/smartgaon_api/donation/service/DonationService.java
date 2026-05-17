package com.smartgaon.ai.smartgaon_api.donation.service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.donation.dto.CampaignResponse;
import com.smartgaon.ai.smartgaon_api.donation.dto.DonateRequest;
import com.smartgaon.ai.smartgaon_api.donation.dto.DonateResponse;
import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignStatus;
import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignType;
import com.smartgaon.ai.smartgaon_api.donation.enums.TransactionStatus;
import com.smartgaon.ai.smartgaon_api.donation.model.*;
import com.smartgaon.ai.smartgaon_api.donation.repository.*;
import com.smartgaon.ai.smartgaon_api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final CampaignRepository campaignRepo;
    private final DonationTransactionRepository txRepo;
    private final DonationRewardRepository rewardRepo;
    private final YearlyDonationCertificateRepository yearlyRepo;
    private final UserRepository userRepo;

    public List<CampaignResponse> getProjects() {

        return campaignRepo
                .findByTypeAndStatus(CampaignType.PROJECT, CampaignStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public List<CampaignResponse> getPrograms() {

        return campaignRepo
                .findByTypeAndStatus(CampaignType.PROGRAM, CampaignStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public DonateResponse donate(Long userId, DonateRequest request){

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DonationCampaign campaign = campaignRepo.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // 🚨 VALIDATION
        double remainingAmount = campaign.getTargetAmount() - campaign.getRaisedAmount();

        if(request.getAmount() > remainingAmount){
            throw new RuntimeException(
                    "Donation exceeds remaining campaign amount. Remaining: " + remainingAmount
            );
        }

        String utr = generateUTR();

        DonationTransaction tx = new DonationTransaction();
        tx.setUser(user);
        tx.setCampaign(campaign);
        tx.setAmount(request.getAmount());
        tx.setUtrNumber(utr);
        tx.setPaymentId("MANUAL");
        tx.setStatus(TransactionStatus.PENDING);
        tx.setDonatedAt(LocalDateTime.now());
        tx.setFinancialYear(calculateFY());

        txRepo.save(tx);

        return DonateResponse.builder()
                .transactionId(tx.getId())
                .utrNumber(utr)
                .message("Donation submitted. Waiting for admin verification")
                .build();
    }


    public List<DonationTransaction> getUserDonations(Long userId){
        return txRepo.findByUserIdOrderByIdDesc(userId);
    }

    public String getReceipt(Long transactionId){

        DonationTransaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if(tx.getStatus() != TransactionStatus.VERIFIED)
            return "NOT_VERIFIED";

        return rewardRepo.findByTransactionId(transactionId)
                .map(DonationReward::getCertificateUrl)
                .orElse("NOT_UPLOADED");
    }

    /* =====================================================
       YEARLY CERTIFICATE
       ===================================================== */

    public String getYearlyCertificate(Long userId, String fy){

        return yearlyRepo.findByUserIdAndFinancialYear(userId, fy)
                .map(YearlyDonationCertificate::getFilePath)
                .orElse("NOT_AVAILABLE");
    }

    private CampaignResponse mapToResponse(DonationCampaign c){

        return CampaignResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .state(c.getState() != null ? c.getState().name() : null)
                .targetAmount(c.getTargetAmount())
                .raisedAmount(c.getRaisedAmount())
                .imageUrl(c.getImageUrl())
                .mediaImages(c.getGalleryImages())
                .build();
    }
  
    
    /* =====================================================
       HELPERS
       ===================================================== */

    private String generateUTR(){
        return "UTR" + System.currentTimeMillis() + (100 + new Random().nextInt(900));
    }

    private String calculateFY(){
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        if(month >= 4)
            return year + "-" + String.valueOf(year+1).substring(2);
        else
            return (year-1) + "-" + String.valueOf(year).substring(2);
    }
}


