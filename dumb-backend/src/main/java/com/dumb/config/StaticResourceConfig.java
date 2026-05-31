package com.dumb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:///d:/work/dumb02/dumb-backend/uploads/");

        registry.addResourceHandler("/videos/**")
            .addResourceLocations("file:///d:/work/dumb02/dumb-frontend/public/videos/");

        registry.addResourceHandler("/audio/**")
            .addResourceLocations("file:///d:/work/dumb02/dumb-frontend/public/audio/");
    }
}
