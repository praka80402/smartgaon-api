package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.service;

import java.util.List;

import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationRequest;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationResponse;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationRoute;

public interface NavigationService {

    /**
     * Detect navigation module from user message
     */
    NavigationResponse resolveNavigation(
            NavigationRequest request);

    /**
     * Get all active modules
     */
    List<NavigationRoute> getAllModules();

    /**
     * Get module by module code
     */
    NavigationRoute getModuleByCode(
            String moduleCode);
}