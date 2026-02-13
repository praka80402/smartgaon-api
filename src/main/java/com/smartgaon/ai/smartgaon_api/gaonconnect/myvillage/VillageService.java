package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.Development;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.DevelopmentRepository;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment.VillageDevelopment;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment.VillageDevelopmentDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VillageService {

    private final VillageRepository repo;
    private final DevelopmentRepository developmentRepo;

    public List<VillageDTO> findAll(){
        return repo.findAll().stream().map(VillageMapper::toDTO).toList();
    }

    public VillageDTO findById(Long id){
        return repo.findById(id).map(VillageMapper::toDTO).orElse(null);
    }


    public void delete(Long id){
        repo.deleteById(id);
    }

    public Map<String, Object> search(int page, int size, String name, String city, String state) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Village> result = repo.searchVillages(
                name != null ? "%" + name + "%" : "%",
                city != null ? "%" + city + "%" : "%",
                state != null ? "%" + state + "%" : "%",
                pageable
        );

        Map<String, Object> response = new HashMap<>();
        response.put("villages", result.getContent().stream().map(VillageMapper::toDTO).toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalPages", result.getTotalPages());
        response.put("totalElements", result.getTotalElements());

        return response;
    }

    public VillageDTO save(VillageDTO dto){

        Village village = dto.getId() != null
                ? repo.findById(dto.getId()).orElse(new Village())
                : new Village();

        village.setName(dto.getName());
        village.setCity(dto.getCity());
        village.setState(dto.getState());
        village.setDescription(dto.getDescription());

        /* ================= IMAGE FIX ================= */
        if(dto.getImages() != null && !dto.getImages().isEmpty()){
            village.setImageFiles(String.join(",", dto.getImages()));
        } else if(dto.getId() == null) {
            // new village with no image
            village.setImageFiles(null);
        }
        /* ============================================ */

        /* ================= DEVELOPMENT FIX ================= */
        /* ================= DEVELOPMENT FIX ================= */
        if (dto.getDevelopments() != null) {

            if (village.getDevelopments() == null) {
                village.setDevelopments(new java.util.ArrayList<>());
            } else {
                village.getDevelopments().clear();
            }

            for (VillageDevelopmentDTO d : dto.getDevelopments()) {

                Development dev = developmentRepo
                        .findById(d.getDevelopmentId())
                        .orElse(null);

                if (dev != null) {

                    // validation
                    Integer percent = d.getProgressPercent() == null ? 0 : d.getProgressPercent();

                    if (percent < 0 || percent > 100)
                        throw new RuntimeException("Progress must be between 0 and 100");

                    VillageDevelopment vd = VillageDevelopment.builder()
                            .village(village)
                            .development(dev)
                            .workDescription(d.getWorkDescription())
                            .benefit(d.getBenefit())
                            .progressPercent(percent)   // ⭐ changed
                            .build();

                    village.getDevelopments().add(vd);
                }
            }
        }
        /* =================================================== */

        /* =================================================== */

        return VillageMapper.toDTO(repo.save(village));
    }
  
}