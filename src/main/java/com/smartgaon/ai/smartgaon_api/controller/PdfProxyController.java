package com.smartgaon.ai.smartgaon_api.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "http://localhost:3000")
public class PdfProxyController {

    @GetMapping("/fetch")
    public ResponseEntity<byte[]> fetchPdf(@RequestParam String url) {
        if (!url.startsWith("https://ncert.nic.in/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid or disallowed URL").getBytes());
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            byte[] pdfBytes = restTemplate.getForObject(url, byte[].class);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("X-Frame-Options", "ALLOWALL");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error fetching PDF: " + e.getMessage()).getBytes());
        }
    }
}
