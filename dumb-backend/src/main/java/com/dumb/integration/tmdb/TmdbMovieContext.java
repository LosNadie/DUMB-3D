package com.dumb.integration.tmdb;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TmdbMovieContext {

    private Integer id;

    private String title;

    private String overview;

    private String posterPath;

    private String backdropPath;

    private String releaseDate;

    private BigDecimal voteAverage;

    private List<String> genres;

    private List<String> productionCountries;
}
