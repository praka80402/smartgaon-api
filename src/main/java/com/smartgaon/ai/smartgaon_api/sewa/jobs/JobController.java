package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping
    public Job create(@RequestBody Job job) {
        return service.create(job);
    }

    @PutMapping("/{id}")
    public Job update(@PathVariable Long id, @RequestBody Job job) {
        return service.update(id, job);
    }

    @GetMapping("/{id}")
    public Job get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Job> all() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
