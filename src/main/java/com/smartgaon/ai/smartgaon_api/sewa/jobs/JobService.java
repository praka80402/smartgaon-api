package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import java.util.List;

public interface JobService {
    Job create(Job job);
    Job update(Long id, Job job);
    void delete(Long id);
    Job get(Long id);
    List<Job> getAll();
}
