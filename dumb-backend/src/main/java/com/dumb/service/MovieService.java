package com.dumb.service;

import com.dumb.dto.request.MovieCreateRequest;
import com.dumb.entity.Movie;

import java.util.List;

public interface MovieService {
    Movie create(MovieCreateRequest request, String username);
    List<Movie> listPublished();
    Movie getById(Long id);
    List<Movie> adminList(String keyword);
    Movie adminGetById(Long id);
    Movie update(Long id, MovieCreateRequest request);
    void deleteById(Long id);
}
