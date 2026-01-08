package com.smartgaon.ai.smartgaon_api.homelayout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeLayoutSectionResponse {

    private String id;        // sectionKey
    private String title;
    private boolean visible;
    private int order;
}
