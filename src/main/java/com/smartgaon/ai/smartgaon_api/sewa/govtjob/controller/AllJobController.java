package com.smartgaon.ai.smartgaon_api.sewa.govtjob.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartgaon.ai.smartgaon_api.sewa.govtjob.entity.AllJobs;
import com.smartgaon.ai.smartgaon_api.sewa.govtjob.service.AllJobService;



@RestController
@RequestMapping("/api/admin/alljobs")
//@CrossOrigin("*")
public class AllJobController {
    @Autowired
    private AllJobService service;

    @PostMapping
    public AllJobs addJob(@RequestBody AllJobs job) {
        return service.save(job);
    }

    @GetMapping
    public List<AllJobs> getJobs() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        service.delete(id);
    }

}
