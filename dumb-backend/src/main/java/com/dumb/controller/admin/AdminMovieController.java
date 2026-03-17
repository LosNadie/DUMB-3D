package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.MovieCreateRequest;
import com.dumb.entity.Movie;
import com.dumb.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movie")
public class AdminMovieController extends BaseController {
    private final MovieService movieService;

    public AdminMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ApiResult<Movie> create(@Valid @RequestBody MovieCreateRequest request) {
        return ApiResult.success(movieService.create(request, currentUsername()));
    }

    @GetMapping
    public ApiResult<List<Movie>> list(@RequestParam(required = false) String keyword) {
        return ApiResult.success(movieService.adminList(keyword));
    }

    @GetMapping("/{id}")
    public ApiResult<Movie> getById(@PathVariable Long id) {
        return ApiResult.success(movieService.adminGetById(id));
    }

    @PutMapping("/{id}")
    public ApiResult<Movie> update(@PathVariable Long id, @Valid @RequestBody MovieCreateRequest request) {
        return ApiResult.success(movieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        movieService.deleteById(id);
        return ApiResult.success(null);
    }
}
