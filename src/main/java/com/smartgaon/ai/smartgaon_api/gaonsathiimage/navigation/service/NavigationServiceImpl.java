package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationRequest;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.NavigationResponse;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationAlias;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationRoute;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.repository.NavigationAliasRepository;
import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.repository.NavigationRouteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NavigationServiceImpl
        implements NavigationService {

    private final NavigationRouteRepository routeRepository;
    private final NavigationAliasRepository aliasRepository;

    @Override
    public NavigationResponse resolveNavigation(
            NavigationRequest request) {

        NavigationResponse response =
                new NavigationResponse();

        NavigationRoute route =
                detectNavigation(
                        request.getMessage());

        if (route != null) {

            response.setSuccess(true);
            response.setModuleCode(
                    route.getModuleCode());

            response.setModuleName(
                    route.getModuleName());

            response.setRoutePath(
                    route.getRoutePath());

            response.setConfidence(100);

            response.setMessage(
                    "Navigation found");

        } else {

            response.setSuccess(false);

            response.setConfidence(0);

            response.setMessage(
                    "No matching module found");
        }

        return response;
    }

    @Override
    public List<NavigationRoute> getAllModules() {

        return routeRepository.findByActiveTrue();
    }

    @Override
    public NavigationRoute getModuleByCode(
            String moduleCode) {

        return routeRepository
                .findByModuleCodeAndActiveTrue(
                        moduleCode)
                .orElse(null);
    }

    // ==========================================
    // HYBRID NAVIGATION
    // AI First + DB Fallback
    // ==========================================

    private NavigationRoute detectNavigation(
            String message) {

        if (message == null ||
                message.isBlank()) {

            return null;
        }

        String normalizedMessage =
                normalizeText(message);

        // =====================================
        // STEP 1 : Future Grok AI
        // =====================================

        try {

            // Example:
            //
            // String moduleCode =
            //      grokService.detectModule(
            //              normalizedMessage);
            //
            // if(moduleCode != null){
            //      return routeRepository
            //              .findByModuleCodeAndActiveTrue(
            //                      moduleCode)
            //              .orElse(null);
            // }

        } catch (Exception e) {

            System.out.println(
                    "Grok failed. Using DB fallback.");
        }

        // =====================================
        // STEP 2 : Alias Matching
        // (longest match wins, not first match)
        // =====================================

        List<NavigationAlias> aliases =
                aliasRepository.findByActiveTrue();

        NavigationAlias bestMatch = null;
        int bestLength = -1;

        for (NavigationAlias alias : aliases) {

            if (alias.getAliasText() == null) {
                continue;
            }

            String aliasText =
                    normalizeText(
                            alias.getAliasText());

            if (aliasText.isEmpty()) {
                continue;
            }

            if (normalizedMessage.contains(aliasText)
                    && aliasText.length() > bestLength) {

                bestMatch = alias;
                bestLength = aliasText.length();
            }
        }

        if (bestMatch != null) {

            return routeRepository
                    .findByModuleCodeAndActiveTrue(
                            bestMatch.getModuleCode())
                    .orElse(null);
        }

        // =====================================
        // STEP 3 : Module Matching
        // =====================================

        List<NavigationRoute> routes =
                routeRepository.findByActiveTrue();

        for (NavigationRoute route : routes) {

            String moduleCode =
                    normalizeText(
                            route.getModuleCode());

            String moduleName =
                    normalizeText(
                            route.getModuleName());

            if (normalizedMessage
                    .contains(moduleCode)
                    || normalizedMessage
                            .contains(moduleName)) {

                return route;
            }
        }

        return null;
    }

    // ==========================================
    // HELPER METHOD
    // ==========================================

    private String normalizeText(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .trim()
                .replaceAll(
                        "[-_]",
                        " ")
                .replaceAll(
                        "[^a-zA-Z0-9\\u0900-\\u097F ]",
                        "")
                .replaceAll(
                        "\\s+",
                        " ");
    }
}