package com.smartgaon.ai.smartgaon_api.homelayout;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeLayoutRepository
        extends JpaRepository<HomeLayout, Long> {

    Optional<HomeLayout> findByScreenAndPlatform(
            String screen,
            String platform
    );
}
