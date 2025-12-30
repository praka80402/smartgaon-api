package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VillageMapper {

    public static VillageDTO toDTO(Village v) {
        VillageDTO dto = new VillageDTO();
        dto.setId(v.getId());
        dto.setName(v.getName());
        dto.setCity(v.getCity());
        dto.setState(v.getState());
        dto.setDescription(v.getDescription());

        if(v.getImageFiles() != null && !v.getImageFiles().isEmpty()) {
            dto.setImages(Arrays.asList(v.getImageFiles().split(",")));
        }
        return dto;
    }

    public static Village toEntity(VillageDTO dto) {
        Village v = new Village();
        v.setId(dto.getId());
        v.setName(dto.getName());
        v.setCity(dto.getCity());
        v.setState(dto.getState());
        v.setDescription(dto.getDescription());

        if(dto.getImages() != null) {
            v.setImageFiles(dto.getImages().stream().collect(Collectors.joining(",")));
        }
        return v;
    }
}