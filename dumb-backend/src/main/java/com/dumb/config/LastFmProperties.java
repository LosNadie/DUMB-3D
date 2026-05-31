package com.dumb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lastfm")
public class LastFmProperties {

    private boolean enabled = true;

    private String apiKey;

    private String baseUrl = "https://ws.audioscrobbler.com/2.0/";

    private boolean autocorrect = true;
}
