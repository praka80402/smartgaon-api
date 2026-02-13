package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

//import java.util.Arrays;
//import java.util.stream.Collectors;
//
//public class VillageMapper {
//
//    public static VillageDTO toDTO(Village v) {
//        VillageDTO dto = new VillageDTO();
//        dto.setId(v.getId());
//        dto.setName(v.getName());
//        dto.setCity(v.getCity());
//        dto.setState(v.getState());
//        dto.setDescription(v.getDescription());
//
//        if(v.getImageFiles() != null && !v.getImageFiles().isEmpty()) {
//            dto.setImages(Arrays.asList(v.getImageFiles().split(",")));
//        }
//        return dto;
//    }
//
//    public static Village toEntity(VillageDTO dto) {
//        Village v = new Village();
//        v.setId(dto.getId());
//        v.setName(dto.getName());
//        v.setCity(dto.getCity());
//        v.setState(dto.getState());
//        v.setDescription(dto.getDescription());
//
//        if(dto.getImages() != null) {
//            v.setImageFiles(dto.getImages().stream().collect(Collectors.joining(",")));
//        }
//        return v;
//    }
//}

//package com.smartgaon.admin.myvillage;
//
//import java.util.Arrays;
//import java.util.stream.Collectors;
//
//public class VillageMapper {
//
//  public static VillageDTO toDTO(Village v) {
//      VillageDTO dto = new VillageDTO();
//      dto.setId(v.getId());
//      dto.setName(v.getName());
//      dto.setCity(v.getCity());
//      dto.setState(v.getState());
//      dto.setDescription(v.getDescription());
//
//      if(v.getImageFiles() != null && !v.getImageFiles().isEmpty()) {
//          dto.setImages(Arrays.asList(v.getImageFiles().split(",")));
//      }
//      return dto;
//  }
//
//  public static Village toEntity(VillageDTO dto) {
//      Village v = new Village();
//      v.setId(dto.getId());
//      v.setName(dto.getName());
//      v.setCity(dto.getCity());
//      v.setState(dto.getState());
//      v.setDescription(dto.getDescription());
//
//      if(dto.getImages() != null) {
//          v.setImageFiles(dto.getImages().stream().collect(Collectors.joining(",")));
//      }
//      return v;
//  }
//}


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment.VillageDevelopmentDTO;

public class VillageMapper {

  public static VillageDTO toDTO(Village v) {

      VillageDTO dto = new VillageDTO();
      dto.setId(v.getId());
      dto.setName(v.getName());
      dto.setCity(v.getCity());
      dto.setState(v.getState());
      dto.setDescription(v.getDescription());

      // Images
      if (v.getImageFiles() != null && !v.getImageFiles().isEmpty()) {
          dto.setImages(Arrays.asList(v.getImageFiles().split(",")));
      }

      // Developments
      if (v.getDevelopments() != null) {

          List<VillageDevelopmentDTO> devDTOList = v.getDevelopments()
                  .stream()
                  .map(dev -> {

                	  VillageDevelopmentDTO d = new VillageDevelopmentDTO();

                	// villageDevelopment table id
                	d.setId(dev.getId());

                	if (dev.getDevelopment() != null) {
                	    d.setDevelopmentId(dev.getDevelopment().getId());
                	    d.setTitle(dev.getDevelopment().getTitle());
                	    d.setDevelopmentDescription(dev.getDevelopment().getDescription());
                	    d.setImageUrl(dev.getDevelopment().getImageUrl());
                	}

                	d.setWorkDescription(dev.getWorkDescription());
                	d.setBenefit(dev.getBenefit());
                	d.setProgressPercent(dev.getProgressPercent());

                	return d;


                  }).collect(Collectors.toList());

          dto.setDevelopments(devDTOList);
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

      if (dto.getImages() != null) {
          v.setImageFiles(String.join(",", dto.getImages()));
      }

      // developments handled in service

      return v;
  }
}

