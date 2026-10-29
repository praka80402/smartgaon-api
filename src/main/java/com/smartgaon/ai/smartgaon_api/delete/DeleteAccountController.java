package com.smartgaon.ai.smartgaon_api.delete;

import com.smartgaon.ai.smartgaon_api.delete.AccountDeleteRequest.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class DeleteAccountController {

    private final AccountDeleteRequestRepository repository;

    public DeleteAccountController(AccountDeleteRequestRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/delete-account/request")
    public ResponseEntity<?> createRequest(@RequestBody AccountDeleteRequest body) {
        if (body.getPhone() == null || !body.getPhone().matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid registered mobile number is required"));
        }
        if (body.getReason() == null || body.getReason().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reason for deletion is required"));
        }
        if (repository.existsByPhoneAndStatus(body.getPhone(), Status.PENDING)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "A deletion request for this account is already pending with admin"));
        }

        body.setId(null);                 // always a new row
        body.setStatus(Status.PENDING);   // user can't set status
        body.setAdminRemark(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(body));
    }

    /** Latest request of the user, to show its status on the page. */
    @GetMapping("/api/delete-account/my-request/{phone}")
    public ResponseEntity<?> myRequest(@PathVariable String phone) {
        return repository.findTopByPhoneOrderByCreatedAtDesc(phone)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}