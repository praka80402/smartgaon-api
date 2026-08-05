package com.smartgaon.ai.smartgaon_api.quick;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class QuickServiceService {

    private final QuickServiceRepository repository;

    public QuickServiceService(QuickServiceRepository repository) {
        this.repository = repository;
    }

    // ---- Public read ----

    public List<QuickService> getActiveServices() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    // ---- Admin reads ----

    public List<QuickService> getAllServices() {
        return repository.findAllByOrderByDisplayOrderAsc();
    }

    // ---- Admin writes ----

    public QuickService create(QuickServiceRequest req) {
        QuickService entity = new QuickService();
        applyRequest(entity, req);

        if (req.getDisplayOrder() == null) {
            int maxOrder = repository.findAllByOrderByDisplayOrderAsc().stream()
                    .mapToInt(QuickService::getDisplayOrder)
                    .max()
                    .orElse(-1);
            entity.setDisplayOrder(maxOrder + 1);
        }

        return repository.save(entity);
    }

    public QuickService update(Long id, QuickServiceRequest req) {
        QuickService entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quick service not found: " + id));
        applyRequest(entity, req);
        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Quick service not found: " + id);
        }
        repository.deleteById(id);
    }

    public QuickService toggleActive(Long id) {
        QuickService entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quick service not found: " + id));
        entity.setActive(!entity.getActive());
        return repository.save(entity);
    }

    public void reorder(ReorderRequest req) {
        for (ReorderItem item : req.getItems()) {
            repository.findById(item.getId()).ifPresent(entity -> {
                entity.setDisplayOrder(item.getDisplayOrder());
                repository.save(entity);
            });
        }
    }

    private void applyRequest(QuickService entity, QuickServiceRequest req) {
        entity.setIcon(req.getIcon());
        entity.setLabel(req.getLabel());
        entity.setSub(req.getSub());
        entity.setPath(req.getPath());
        if (req.getDisplayOrder() != null) {
            entity.setDisplayOrder(req.getDisplayOrder());
        }
        entity.setActive(req.getActive() != null ? req.getActive() : true);
        entity.setIsCustom(req.getIsCustom() != null ? req.getIsCustom() : true);
    }
}
