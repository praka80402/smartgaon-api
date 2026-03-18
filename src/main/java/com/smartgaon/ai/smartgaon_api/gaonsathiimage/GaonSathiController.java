package com.smartgaon.ai.smartgaon_api.gaonsathiimage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/gaon-sathi")
public class GaonSathiController {

    private final GaonSathiRepository repository;

    public GaonSathiController(GaonSathiRepository repository) {
        this.repository = repository;
    }
    @GetMapping("/images")
    public ResponseEntity<List<Gaon_Sathi_Image>> getImages() {
    return ResponseEntity.ok(repository.findAll());
}
}
