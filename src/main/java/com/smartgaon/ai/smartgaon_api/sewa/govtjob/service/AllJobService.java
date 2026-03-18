package com.smartgaon.ai.smartgaon_api.sewa.govtjob.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.sewa.govtjob.entity.AllJobs;
import com.smartgaon.ai.smartgaon_api.sewa.govtjob.repository.AllJobRepository;

@Service
public class AllJobService {
    @Autowired
    private AllJobRepository repo;

    public AllJobs save(AllJobs job) {
        return repo.save(job);
    }

    public List<AllJobs> getAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

}
