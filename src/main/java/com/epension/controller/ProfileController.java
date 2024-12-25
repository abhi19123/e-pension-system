package com.epension.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private static final String PROFILE_PHOTO_DIR = "path/to/profile/photos/";
    private static final String SIGNATURE_DIR = "path/to/signatures/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/uploadProfilePhoto")
    public ResponseEntity<String> uploadProfilePhoto(@RequestParam("profilePhoto") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("Invalid file or file size exceeds limit.");
        }

        String fileType = file.getContentType();
        if (!fileType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("File must be an image.");
        }

        try {
            file.transferTo(new File(PROFILE_PHOTO_DIR + file.getOriginalFilename()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving file.");
        }

        return ResponseEntity.ok("Profile photo uploaded successfully");
    }

    @PostMapping("/uploadDigitalSignature")
    public ResponseEntity<String> uploadDigitalSignature(@RequestParam("digitalSignature") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("Invalid file or file size exceeds limit.");
        }

        String fileType = file.getContentType();
        if (!fileType.equals("image/png") && !fileType.equals("image/svg+xml")) {
            return ResponseEntity.badRequest().body("File must be a PNG or SVG.");
        }

        try {
            file.transferTo(new File(SIGNATURE_DIR + file.getOriginalFilename()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving file.");
        }

        return ResponseEntity.ok("Digital signature uploaded successfully");
    }
}