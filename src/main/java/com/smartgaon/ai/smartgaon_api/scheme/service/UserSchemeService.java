package com.smartgaon.ai.smartgaon_api.scheme.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.scheme.entity.*;
import com.smartgaon.ai.smartgaon_api.scheme.repository.*;
import java.time.LocalDateTime;

import com.smartgaon.ai.smartgaon_api.scheme.dto.InterestedRequest;
import com.smartgaon.ai.smartgaon_api.scheme.entity.InterestedUser;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSchemeService {

    private final UserSchemeRepository schemeRepo;
    private final UserCategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final InterestedUserRepository interestedRepo;


    // ================= CATEGORIES =================
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    // ================= CENTRAL SCHEMES =================
    public List<Scheme> getCentralSchemes(Long categoryId) {
        return schemeRepo.findByCategoryIdAndSchemeType(
                categoryId,
                SchemeType.CENTRAL
        );
    }
    public Scheme getSchemeDetail(Long schemeId) {
        return schemeRepo.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Scheme not found"));
    }


    // ================= STATE SCHEMES (FROM USER PROFILE) =================
    public List<Scheme> getStateSchemesByState(Long categoryId, State state) {
        return schemeRepo.findByCategoryIdAndSchemeTypeAndState(
                categoryId,
                SchemeType.STATE,
                state
        );
    }
    
    
//    ------------------------------------
    
//    public void addInterestedUser(Long schemeId, InterestedRequest request) {
//
//        Scheme scheme = schemeRepo.findById(schemeId)
//                .orElseThrow(() -> new RuntimeException("Scheme not found"));
//
//        // Save interested user
//        InterestedUser user = InterestedUser.builder()
//                .name(request.getName())
//                .village(request.getVillage())
//                .pincode(request.getPincode())
//                .phoneNumber(request.getPhoneNumber())
//                .date(LocalDateTime.now())
//                .scheme(scheme)
//                .build();
//
//        interestedRepo.save(user);
//
//        // Increase count
//        if (scheme.getInterestCount() == null) {
//            scheme.setInterestCount(0L);
//        }
//
//        scheme.setInterestCount(scheme.getInterestCount() + 1);
//
//        schemeRepo.save(scheme);
//    }
    
    public void addInterestedUser(Long schemeId, InterestedRequest request) {

        if (
            request.getName() == null || request.getName().isEmpty() ||
            request.getVillage() == null || request.getVillage().isEmpty() ||
            request.getPincode() == null || request.getPincode().isEmpty() ||
            request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()
        ) {
            throw new RuntimeException("Incomplete user details");
        }

        Scheme scheme = schemeRepo.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Scheme not found"));

        InterestedUser user = InterestedUser.builder()
                .name(request.getName())
                .village(request.getVillage())
                .pincode(request.getPincode())
                .phoneNumber(request.getPhoneNumber())
                .date(LocalDateTime.now())
                .scheme(scheme)
                .build();

        interestedRepo.save(user);

        if (scheme.getInterestCount() == null) {
            scheme.setInterestCount(0L);
        }

        scheme.setInterestCount(scheme.getInterestCount() + 1);

        schemeRepo.save(scheme);
    }

    public boolean isAlreadyInterested(Long schemeId, String phone) {
        return interestedRepo.existsBySchemeIdAndPhoneNumber(schemeId, phone);
    }




}
