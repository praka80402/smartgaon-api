package com.smartgaon.ai.smartgaon_api.suggestion.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartgaon.ai.smartgaon_api.suggestion.Entity.Suggestion;
import com.smartgaon.ai.smartgaon_api.suggestion.Repository.SuggestionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository repo;

    public Suggestion submit(Suggestion s) {
        return repo.save(s);
    }

    public List<Suggestion> getByPincode(String pincode) {
        return repo.findByPincodeOrderByCreatedAtDesc(pincode);
    }

    public List<Suggestion> getByPhone(String phone) {
        return repo.findByPhoneOrderByCreatedAtDesc(phone);
    }

    public Optional<Suggestion> getById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Suggestion update(Long id, Suggestion update) {
        return repo.findById(id).map(existing -> {
            if (update.getTitle() != null) existing.setTitle(update.getTitle());
            if (update.getDescription() != null) existing.setDescription(update.getDescription());
            if (update.getStatus() != null) existing.setStatus(update.getStatus());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Suggestion not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

