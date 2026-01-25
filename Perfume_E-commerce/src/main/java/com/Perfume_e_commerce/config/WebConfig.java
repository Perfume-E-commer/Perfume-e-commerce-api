package com.Perfume_e_commerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {
    private static final String UPLOAD_DIR = "/home/dararith/perfume-uploads";
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory(registry);
    }

    private void exposeDirectory(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(UPLOAD_DIR);

        if (!uploadDir.toFile().exists()) {
            uploadDir.toFile().mkdirs();
        }

        String uploadPath = uploadDir.toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadPath);
    }
}