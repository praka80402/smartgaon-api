package com.smartgaon.ai.smartgaon_api.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BusinessResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String budget;
    private List<String> images;
    private String status;
}
