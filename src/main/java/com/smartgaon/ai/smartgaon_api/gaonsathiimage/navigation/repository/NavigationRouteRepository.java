package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationRoute;

@Repository
public interface NavigationRouteRepository
        extends JpaRepository<NavigationRoute, Long> {

    Optional<NavigationRoute>
            findByModuleCodeAndActiveTrue(
                    String moduleCode);

    List<NavigationRoute> findByActiveTrue();
}