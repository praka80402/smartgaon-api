package com.smartgaon.ai.smartgaon_api.homelayout;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeLayoutController {

    private final HomeLayoutService service;

    @GetMapping("/home-layout")
    public List<HomeLayoutSectionResponse> getHomeLayout() {
        return service.getHomeLayout();
    }

}
