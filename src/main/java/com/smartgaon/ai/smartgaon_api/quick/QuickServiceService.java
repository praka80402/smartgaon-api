package com.smartgaon.ai.smartgaon_api.quick;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class QuickServiceService {

    private final QuickServiceRepository repository;
    private final QuickServiceMobileRepository mobRepository;

    public QuickServiceService(QuickServiceRepository repository, QuickServiceMobileRepository mobRepository) {
        this.repository = repository;
        this.mobRepository = mobRepository;
    }

    // ---- WEB ----
    public List<QuickService> getActiveServices() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc();
    }
    public List<QuickService> getAllServices() {
        return repository.findAllByOrderByDisplayOrderAsc();
    }
    public QuickService create(QuickServiceRequest req) {
        QuickService entity = new QuickService();
        applyRequest(entity, req);
        if (req.getDisplayOrder() == null) {
            int maxOrder = repository.findAllByOrderByDisplayOrderAsc().stream()
                    .mapToInt(QuickService::getDisplayOrder).max().orElse(-1);
            entity.setDisplayOrder(maxOrder + 1);
        }
        return repository.save(entity);
    }
    public QuickService update(Long id, QuickServiceRequest req) {
        QuickService entity = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found: " + id));
        applyRequest(entity, req);
        return repository.save(entity);
    }
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new NoSuchElementException("Not found: " + id);
        repository.deleteById(id);
    }
    public QuickService toggleActive(Long id) {
        QuickService entity = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found: " + id));
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

    // ---- MOB ----
    public List<QuickServiceMobile> getActiveMobServices() {
        return mobRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }
    public List<QuickServiceMobile> getAllMobServices() {
        return mobRepository.findAllByOrderByDisplayOrderAsc();
    }
    public QuickServiceMobile createMob(QuickServiceMobileRequest req) {
        QuickServiceMobile entity = new QuickServiceMobile();
        applyMobRequest(entity, req);
        if (req.getDisplayOrder() == null) {
            int maxOrder = mobRepository.findAllByOrderByDisplayOrderAsc().stream()
                    .mapToInt(QuickServiceMobile::getDisplayOrder).max().orElse(-1);
            entity.setDisplayOrder(maxOrder + 1);
        }
        return mobRepository.save(entity);
    }
    public QuickServiceMobile updateMob(Long id, QuickServiceMobileRequest req) {
        QuickServiceMobile entity = mobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found: " + id));
        applyMobRequest(entity, req);
        return mobRepository.save(entity);
    }
    public void deleteMob(Long id) {
        if (!mobRepository.existsById(id)) throw new NoSuchElementException("Not found: " + id);
        mobRepository.deleteById(id);
    }
    public QuickServiceMobile toggleActiveMob(Long id) {
        QuickServiceMobile entity = mobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found: " + id));
        entity.setActive(!entity.getActive());
        return mobRepository.save(entity);
    }
    public void reorderMob(ReorderRequest req) {
        for (ReorderItem item : req.getItems()) {
            mobRepository.findById(item.getId()).ifPresent(entity -> {
                entity.setDisplayOrder(item.getDisplayOrder());
                mobRepository.save(entity);
            });
        }
    }

    private void applyRequest(QuickService entity, QuickServiceRequest req) {
        entity.setIcon(req.getIcon());
        entity.setLabel(req.getLabel());
        entity.setSub(req.getSub());
        entity.setPath(req.getPath());
        if (req.getDisplayOrder() != null) entity.setDisplayOrder(req.getDisplayOrder());
        entity.setActive(req.getActive() != null ? req.getActive() : true);
        entity.setIsCustom(req.getIsCustom() != null ? req.getIsCustom() : true);
    }

    private void applyMobRequest(QuickServiceMobile entity, QuickServiceMobileRequest req) {
        entity.setIcon(req.getIcon());
        entity.setLabel(req.getLabel());
        entity.setSub(req.getSub());
        entity.setPath(req.getPath());
        if (req.getDisplayOrder() != null) entity.setDisplayOrder(req.getDisplayOrder());
        entity.setActive(req.getActive() != null ? req.getActive() : true);
        entity.setIsCustom(req.getIsCustom() != null ? req.getIsCustom() : true);
    }
}
