package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SgPopularPlaceDTO {
    private Long id;
    private String name;
    private String description;
    private List<String> photos = new ArrayList<>();
    private String videoUrl;
}