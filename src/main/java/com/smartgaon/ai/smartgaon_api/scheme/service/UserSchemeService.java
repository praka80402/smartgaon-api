package com.smartgaon.ai.smartgaon_api.scheme.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.scheme.entity.*;
import com.smartgaon.ai.smartgaon_api.scheme.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSchemeService {

    private final UserSchemeRepository schemeRepo;
    private final UserCategoryRepository categoryRepo;
    private final UserRepository userRepo;

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

}
