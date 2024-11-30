package com.springboot.blog.controller;

import com.springboot.blog.entity.Logo;
import com.springboot.blog.service.LogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Logo-v2")
@RestController
@RequestMapping("/api/v2/logo")
public class LogoController {

        @Autowired
        private LogoService logoService;

        @Operation(summary = "Upload a new logo", description = "Uploads a logo with a title to the database.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"))))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Logo uploaded successfully!"),
                        @ApiResponse(responseCode = "500", description = "Error uploading file")
        })
        @PostMapping("/upload")
        public ResponseEntity<?> uploadLogo(
                        @RequestParam("id") Long id,
                        @RequestParam("title") String title,
                        @RequestParam("logo") MultipartFile logoFile) {
                try {
                        Logo logo = logoService.uploadLogo(id, title, logoFile);
                        return ResponseEntity.ok("Logo uploaded successfully!");
                } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error uploading file: " + e.getMessage());
                }
        }

        @Operation(summary = "Get logo by ID", description = "Retrieves a logo entity by its ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Logo retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Logo not found for the given ID")
        })
        @GetMapping("/{id}")
        public ResponseEntity<?> getLogo(@PathVariable Long id) {
                Logo logo = logoService.getLogoById(id);
                if (logo == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("Logo not found for id: " + id);
                }
                return ResponseEntity.ok(logo);
        }

        @Operation(summary = "Get logo image", description = "Retrieves the binary image data of a logo by its ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Logo image retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Logo not found for the given ID")
        })
        @GetMapping("/{id}/image")
        public ResponseEntity<byte[]> getLogoImage(@PathVariable Long id) {
                byte[] imageData = logoService.getLogoImageById(id);
                if (imageData == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                return ResponseEntity.ok()
                                .header("Content-Type", "image/png")
                                .body(imageData);
        }

        @Operation(summary = "Update logo by ID", description = "Updates an existing logo's title and image by its ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Logo updated successfully!"),
                        @ApiResponse(responseCode = "404", description = "Logo not found for the given ID"),
                        @ApiResponse(responseCode = "500", description = "Error updating logo")
        })
        @PutMapping("/{id}")
        public ResponseEntity<?> updateLogo(
                        @PathVariable Long id,
                        @RequestParam("title") String title,
                        @RequestParam("logo") MultipartFile logoFile) {
                try {
                        Logo updatedLogo = logoService.updateLogo(id, title, logoFile);
                        if (updatedLogo == null) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body("Logo not found for id: " + id);
                        }
                        return ResponseEntity.ok("Logo updated successfully!");
                } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error updating logo: " + e.getMessage());
                }
        }
}