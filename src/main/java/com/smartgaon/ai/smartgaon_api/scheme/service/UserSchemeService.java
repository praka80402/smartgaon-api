package com.smartgaon.ai.smartgaon_api.scheme.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.scheme.entity.*;
import com.smartgaon.ai.smartgaon_api.scheme.repository.*;
import java.time.LocalDateTime;

import com.smartgaon.ai.smartgaon_api.scheme.dto.InterestedRequest;
import com.smartgaon.ai.smartgaon_api.scheme.dto.UserCategoryResponse;
import com.smartgaon.ai.smartgaon_api.scheme.dto.UserSchemeResponse;
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
    @Transactional(readOnly = true)
    public List<UserCategoryResponse> getAllCategories(Language lang) {
        return categoryRepo.findAll().stream()
                .map(c -> UserCategoryResponse.of(c, lang))
                .toList();
    }

    // ================= CENTRAL SCHEMES =================
    @Transactional(readOnly = true)
    public List<UserSchemeResponse> getCentralSchemes(Long categoryId, Language lang) {
        return schemeRepo.findByCategoryIdAndSchemeType(categoryId, SchemeType.CENTRAL)
                .stream()
                .map(s -> UserSchemeResponse.of(s, lang))
                .toList();
    }

    // ================= SCHEME DETAIL =================
    @Transactional(readOnly = true)
    public UserSchemeResponse getSchemeDetail(Long schemeId, Language lang) {
        Scheme scheme = schemeRepo.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Scheme not found"));
        return UserSchemeResponse.of(scheme, lang);
    }

    // ================= STATE SCHEMES (FROM USER PROFILE) =================
    @Transactional(readOnly = true)
    public List<UserSchemeResponse> getStateSchemesByState(Long categoryId, State state, Language lang) {
        return schemeRepo.findByCategoryIdAndSchemeTypeAndState(categoryId, SchemeType.STATE, state)
                .stream()
                .map(s -> UserSchemeResponse.of(s, lang))
                .toList();
    }
    
    
    // ---- Old English-only methods, kept in case other code still calls them ----
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public List<Scheme> getCentralSchemes(Long categoryId) {
        return schemeRepo.findByCategoryIdAndSchemeType(categoryId, SchemeType.CENTRAL);
    }

    public Scheme getSchemeDetail(Long schemeId) {
        return schemeRepo.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Scheme not found"));
    }

    public List<Scheme> getStateSchemesByState(Long categoryId, State state) {
        return schemeRepo.findByCategoryIdAndSchemeTypeAndState(categoryId, SchemeType.STATE, state);
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
