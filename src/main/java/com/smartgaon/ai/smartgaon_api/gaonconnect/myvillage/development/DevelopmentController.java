package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/development")
@RequiredArgsConstructor
public class DevelopmentController {

    private final DevelopmentService service;
    private final DevelopmentRepository repo;

    /* ================= CREATE ================= */

    /* ================= GET BY PHASE ================= */
    @GetMapping("/phase/{phaseNumber}")
    public List<Development> getByPhaseNumber(@PathVariable Integer phaseNumber) {
        return repo.findByPhaseNumberOrderByIdDesc(phaseNumber);
    }

    /* ================= UPDATE ================= */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Development update(
            @PathVariable Long id,
            @RequestParam Integer phaseNumber,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam PhaseStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {

        Development dev = new Development();
        dev.setPhaseNumber(phaseNumber);
        dev.setTitle(title);
        dev.setDescription(description);
        dev.setStatus(status);

        if (startDate != null && !startDate.isEmpty())
            dev.setStartDate(LocalDate.parse(startDate));

        if (endDate != null && !endDate.isEmpty())
            dev.setEndDate(LocalDate.parse(endDate));

        return service.update(id, dev);
    }

    /* ================= GET ALL ================= */
    @GetMapping
    public List<Development> getAll() {
        return service.getAll();
    }

    /* ================= FILTER BY STATUS ================= */
    @GetMapping("/status/{status}")
    public List<Development> getByStatus(@PathVariable PhaseStatus status) {
        return service.getByStatus(status);
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}