package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation.entity.NavigationAlias;

@Repository
public interface NavigationAliasRepository
        extends JpaRepository<NavigationAlias, Long> {

    List<NavigationAlias> findByActiveTrue();

    List<NavigationAlias>
            findByModuleCodeAndActiveTrue(
                    String moduleCode);
}