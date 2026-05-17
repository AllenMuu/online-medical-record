package com.ice.medicalrecord.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    private final String avatarResourceLocation;

    public StaticResourceConfig(@Value("${app.storage.avatar-dir:backend/uploads/avatars}") String avatarDir) {
        Path storagePath = Path.of(avatarDir).toAbsolutePath().normalize();
        this.avatarResourceLocation = storagePath.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarResourceLocation.endsWith("/") ? avatarResourceLocation : avatarResourceLocation + "/");
    }
}
