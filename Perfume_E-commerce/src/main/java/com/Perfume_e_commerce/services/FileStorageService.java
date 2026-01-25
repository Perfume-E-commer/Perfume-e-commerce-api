package com.Perfume_e_commerce.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final String SERVER_UPLOAD_DIR = "/home/dararith/perfume-uploads";
    private final Path fileStorageLocation ;

    public FileStorageService() {
        Path targetPath;

        try {
            Path serverPath = Paths.get(SERVER_UPLOAD_DIR);
            if (!Files.exists(serverPath)) {
                Files.createDirectories(serverPath);
            }
            if (Files.isWritable(serverPath)) {
                targetPath = serverPath;
            } else {
                System.out.println("Warning: Server path exists but is not writable. Falling back to local 'uploads'.");
                targetPath = Paths.get("uploads");
            }
        } catch (Exception e) {
            System.out.println("Notice: Could not use server path (" + SERVER_UPLOAD_DIR + "). Falling back to local 'uploads'.");
            targetPath = Paths.get("uploads");
        }

        this.fileStorageLocation = targetPath.toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("File Storage configured at: " + this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        String fileName = originalFileName.replaceAll("\\s+", "_");

        try {
            if(fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            String uniqueFileName = UUID.randomUUID().toString() + "-" + fileName;

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/api/uploads/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Path loadFile(String filename) {
        return fileStorageLocation.resolve(filename);
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + filename, ex);
        }
    }
}
