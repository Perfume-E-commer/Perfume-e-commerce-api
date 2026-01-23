package com.Perfume_e_commerce.controllers;
import com.Perfume_e_commerce.services.EmailService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private EmailService emailService;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @PostMapping
    public ResponseEntity<?> sendContactMessage(@RequestBody ContactRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        String cleanEmail = request.getEmail().trim();

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format. Please check for spaces or typos (e.g., name@example.com)");
        }

        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().body("Message is required");
        }

        try {
            emailService.sendContactMessage(
                    request.getName(),
                    cleanEmail,
                    request.getSubject(),
                    request.getMessage()
            );
            return ResponseEntity.ok("Message sent successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
    }

    @Data
    public static class ContactRequest {
        private String name;
        private String email;
        private String subject;
        private String message;
    }
}
