
package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VillageService {

    private final VillageRepository repo;

    /* ================= FIND ALL ================= */
    public List<VillageDTO> findAll() {
        return repo.findAll()
                .stream()
                .map(VillageMapper::toDTO)
                .toList();
    }

    /* ================= FIND BY ID ================= */
    public VillageDTO findById(Long id) {
        return repo.findById(id)
                .map(VillageMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Village not found with id: " + id));
    }

    /* ================= DELETE ================= */
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Village not found with id: " + id);
        }
        repo.deleteById(id);
    }

    /* ================= SEARCH WITH PAGINATION ================= */
    public Map<String, Object> search(int page, int size,
                                      String name,
                                      String city,
                                      String state) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Village> result = repo.searchVillages(
                name != null && !name.isBlank() ? "%" + name + "%" : "%",
                city != null && !city.isBlank() ? "%" + city + "%" : "%",
                state != null && !state.isBlank() ? "%" + state + "%" : "%",
                pageable
        );

        Map<String, Object> response = new HashMap<>();
        response.put("villages",
                result.getContent().stream()
                        .map(VillageMapper::toDTO)
                        .toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalPages", result.getTotalPages());
        response.put("totalElements", result.getTotalElements());

        return response;
    }

    /* ================= SAVE / UPDATE ================= */
    public VillageDTO save(VillageDTO dto) {

        Village village;

        if (dto.getId() != null) {
            village = repo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Village not found"));
        } else {
            village = new Village();
        }

        village.setName(dto.getName());
        village.setCity(dto.getCity());
        village.setState(dto.getState());
        village.setDescription(dto.getDescription());
        village.setSmartGaon(dto.getSmartGaon() != null ? dto.getSmartGaon() : false);

        /* ========= IMAGE HANDLING ========= */
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            village.setImageFiles(String.join(",", dto.getImages()));
        }
        // If updating and images are null → keep old images
        /* ================================== */

        Village saved = repo.save(village);
        return VillageMapper.toDTO(saved);
    }

    /* ================= SMART GAON LIST ================= */
    public List<VillageDTO> getSmartVillages() {
        return repo.findBySmartGaonTrue()
                .stream()
                .map(VillageMapper::toDTO)
                .toList();
    }
    
    public List<VillageDTO> getMyVillage(String state, String district, String area) {

        List<Village> villages =
                repo.findByStateAndDistrictAndArea(state, district, area);

        return villages.stream()
                .map(VillageMapper::toDTO)
                .toList();
    }
}