package com.dumb.integration.lastfm;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class LastFmAlbumContext {

    String albumName;

    String artistName;

    String releaseDate;

    String releaseYear;

    String url;

    String mbid;

    String coverImageUrl;

    List<String> trackNames;
}
