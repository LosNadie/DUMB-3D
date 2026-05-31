package com.dumb;

import com.dumb.config.AiProperties;
import com.dumb.config.LastFmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AiProperties.class, LastFmProperties.class})
public class DumbBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DumbBackendApplication.class, args);
    }
}
