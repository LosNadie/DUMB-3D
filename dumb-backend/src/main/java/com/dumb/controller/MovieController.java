package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.entity.Movie;
import com.dumb.service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ApiResult<List<Movie>> list() {
        return ApiResult.success(movieService.listPublished());
    }

    @GetMapping("/{id}")
    public ApiResult<Movie> detail(@PathVariable Long id) {
        return ApiResult.success(movieService.getById(id));
    }
}
