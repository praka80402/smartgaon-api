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

}