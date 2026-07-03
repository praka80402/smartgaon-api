package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import lombok.Data;

@Data
public class SgPopularPlaceDTO {
    private Long id;
    private String name;
    private String description;
    private String photo;
    private String videoUrl;
}
