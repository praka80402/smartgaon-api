package com.smartgaon.ai.smartgaon_api.homelayout;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
    @RequiredArgsConstructor
    @Transactional
    public class HomeLayoutService {

        private final HomeLayoutRepository layoutRepo;
        private final HomeLayoutSectionRepository sectionRepo;

        /* MOBILE */
        public List<HomeLayoutSectionResponse> getHomeLayout() {

            HomeLayout layout = layoutRepo
                    .findByScreenAndPlatform("home", "mobile")
                    .orElseThrow(() -> new RuntimeException("Home layout not found"));

            return sectionRepo.findByLayout(layout).stream()
                    .filter(HomeLayoutSection::getVisible)
                    .sorted(Comparator.comparing(
                            HomeLayoutSection::getDisplayOrder))
                    .map(s -> new HomeLayoutSectionResponse(
                            s.getSectionKey(),
                            s.getTitle(),
                            s.getVisible(),
                            s.getDisplayOrder()
                    ))
                    .toList();
        }

}



