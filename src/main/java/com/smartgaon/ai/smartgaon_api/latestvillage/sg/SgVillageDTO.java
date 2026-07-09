package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.List;
import lombok.Data;

@Data
public class SgVillageDTO {
    private Long id;
    private String name;
    private String district;
    private String state;
    private String pincode;
    private String description;
    private List<String> images;
    private Boolean popularPlace;
    private List<SgPopularPlaceDTO> popularPlaces;
    private Boolean smartGaon;
    private Boolean stayEnquiry;
    private List<SgAssignmentDTO> assignments;
}
