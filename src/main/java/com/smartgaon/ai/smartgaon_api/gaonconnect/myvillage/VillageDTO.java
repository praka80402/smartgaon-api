package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import lombok.Data;

import java.util.List;

@Data
public class VillageDTO {
    private Long id;
    private String name;
    private String city;
    private String state;
    private String description;
    private List<String> images;
}