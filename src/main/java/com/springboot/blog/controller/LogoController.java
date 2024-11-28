package com.springboot.blog.controller;

import com.springboot.blog.entity.Logo;
import com.springboot.blog.repository.LogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/logo")
public class LogoController {

    @Autowired
    private LogoRepository logoRepository;

    // Upload a new logo
    @PostMapping("/upload")
    public ResponseEntity<?> uploadLogo(
            @RequestParam("title") String title,
            @RequestParam("logo") MultipartFile logoFile) {
        try {
            // Create a new Logo entity
            Logo logo = new Logo();
            logo.setTitle(title);
            logo.setLogoData(logoFile.getBytes()); // Set the binary data
            logoRepository.save(logo);

            return ResponseEntity.ok("Logo uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    // Get logo by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogo(@PathVariable Long id) {
        Logo logo = logoRepository.findById(id).orElse(null);

        if (logo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Logo not found for id: " + id);
        }

        return ResponseEntity.ok(logo);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getLogoImage(@PathVariable Long id) {
        Logo logo = logoRepository.findById(id).orElse(null);

        if (logo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        // Set headers to indicate the image type (assuming PNG, adjust as needed)
        return ResponseEntity.ok()
                .header("Content-Type", "image/png") // Adjust MIME type as per your image format
                .body(logo.getLogoData());
    }


    // Update logo by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLogo(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("logo") MultipartFile logoFile) {
        try {
            Logo existingLogo = logoRepository.findById(id).orElse(null);

            if (existingLogo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Logo not found for id: " + id);
            }

            // Update title and binary data
            existingLogo.setTitle(title);
            existingLogo.setLogoData(logoFile.getBytes());
            logoRepository.save(existingLogo);

            return ResponseEntity.ok("Logo updated successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating logo: " + e.getMessage());
        }
    }
}
