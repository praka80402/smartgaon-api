package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DevelopmentService {

    private final DevelopmentRepository repo;

    public Development create(Development dev) {
        return repo.save(dev);
    }

    public Development update(Long id, Development dev) {

        Development existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Phase not found"));

        existing.setPhaseNumber(dev.getPhaseNumber());
        existing.setTitle(dev.getTitle());
        existing.setDescription(dev.getDescription());
        existing.setStatus(dev.getStatus());
        existing.setStartDate(dev.getStartDate());
        existing.setEndDate(dev.getEndDate());

        return repo.save(existing);
    }

    public List<Development> getAll() {
        return repo.findAllByOrderByPhaseNumberAsc();
    }

    public List<Development> getByStatus(PhaseStatus status) {
        return repo.findByStatus(status);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}