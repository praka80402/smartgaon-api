package com.smartgaon.ai.smartgaon_api.homelayout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeLayoutSectionDto {

    private String sectionKey;
    private String title;
    private Boolean visible;
    private Integer displayOrder;
}
