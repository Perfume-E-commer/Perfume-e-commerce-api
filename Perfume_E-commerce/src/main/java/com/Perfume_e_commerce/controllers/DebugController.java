package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.user.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final Path uploadsDir = Paths.get("uploads");

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/uploads")
    public ResponseEntity<?> listUploads() {
        try {
            if (!Files.exists(uploadsDir)) {
                return ResponseEntity.ok(Map.of("files", List.of()));
            }

            List<String> files = Files.list(uploadsDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("files", files));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserImageUrl(@PathVariable String id) {
        try {
            ObjectId oid = new ObjectId(id);
            return userRepository.findById(oid)
                    .map(user -> ResponseEntity.ok(Map.of("imageUrl", user.getImageUrl())))
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid id"));
        }
    }

}
