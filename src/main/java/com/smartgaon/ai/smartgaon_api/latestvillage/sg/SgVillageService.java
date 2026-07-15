package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/*
 * Read-only service exposing the admin-created sg_village data to the website.
 * @Transactional(readOnly=true) keeps the session open so EAGER children load
 * safely (prevents lazy-loading 500s).
 */
@Service
@RequiredArgsConstructor
public class SgVillageService {

    private final SgVillageRepository villageRepo;
    private final SgDevelopmentRepository developmentRepo;

    @Transactional(readOnly = true)
    public List<SgVillageDTO> getSmartVillages() {
        Map<Long, SgDevelopment> catalogue = catalogueById();
        return villageRepo.findBySmartGaonTrue()
                .stream().map(v -> toDTO(v, catalogue))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SgVillageDTO> getAll() {
        Map<Long, SgDevelopment> catalogue = catalogueById();
        return villageRepo.findAll()
                .stream().map(v -> toDTO(v, catalogue))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SgVillageDTO getById(Long id) {
        return villageRepo.findById(id)
                .map(v -> toDTO(v, catalogueById()))
                .orElse(null);
    }

    private Map<Long, SgDevelopment> catalogueById() {
        return developmentRepo.findAll().stream()
                .collect(Collectors.toMap(SgDevelopment::getId, Function.identity(), (a, b) -> a));
    }

    private SgVillageDTO toDTO(SgVillage v, Map<Long, SgDevelopment> catalogue) {
        SgVillageDTO dto = new SgVillageDTO();
        dto.setId(v.getId());
        dto.setName(v.getName());
        dto.setDistrict(v.getDistrict());
        dto.setState(v.getState());
        dto.setDescription(v.getDescription());
        dto.setImages(v.getImages() != null ? new ArrayList<>(v.getImages()) : new ArrayList<>());
        dto.setPopularPlace(v.getPopularPlace());
        dto.setSmartGaon(v.getSmartGaon());
        dto.setStayEnquiry(v.getStayEnquiry());

        List<SgPopularPlaceDTO> places = new ArrayList<>();
        if (v.getPopularPlaces() != null) {
            for (SgPopularPlace pp : v.getPopularPlaces()) {
                SgPopularPlaceDTO pd = new SgPopularPlaceDTO();
                pd.setId(pp.getId());
                pd.setName(pp.getName());
                pd.setDescription(pp.getDescription());
                pd.setPhotos(pp.getPhotos());
                pd.setVideoUrl(pp.getVideoUrl());
                places.add(pd);
            }
        }
        dto.setPopularPlaces(places);

        List<SgAssignmentDTO> assigns = new ArrayList<>();
        if (v.getAssignments() != null) {
            for (SgVillageDevelopment vd : v.getAssignments()) {
                SgAssignmentDTO ad = new SgAssignmentDTO();
                ad.setId(vd.getId());
                ad.setDevelopmentId(vd.getDevelopmentId());
                ad.setProgressPercent(vd.getProgressPercent());
                ad.setImages(vd.getImages() != null ? new ArrayList<>(vd.getImages()) : new ArrayList<>());
                ad.setVideoUrl(vd.getVideoUrl());
                ad.setDocument(vd.getDocument());

                SgDevelopment d = catalogue.get(vd.getDevelopmentId());
                if (d != null) {
                    SgDevelopmentDTO dd = new SgDevelopmentDTO();
                    dd.setId(d.getId());
                    dd.setPhaseNumber(d.getPhaseNumber());
                    dd.setTitle(d.getTitle());
                    dd.setDescription(d.getDescription());
                    dd.setImage(d.getImage());
                    ad.setDevelopment(dd);
                }
                assigns.add(ad);
            }
        }
        dto.setAssignments(assigns);
        return dto;
    }
}
