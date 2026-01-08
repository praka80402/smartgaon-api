package com.smartgaon.ai.smartgaon_api.homelayout;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeLayoutSectionRepository
        extends JpaRepository<HomeLayoutSection, Long> {

    List<HomeLayoutSection> findByLayout(HomeLayout layout);

    Optional<HomeLayoutSection> findByLayoutAndSectionKey(
            HomeLayout layout,
            String sectionKey
    );
}
