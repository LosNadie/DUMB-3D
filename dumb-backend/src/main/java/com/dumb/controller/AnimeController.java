package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.entity.Anime;
import com.dumb.service.AnimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
public class AnimeController {
    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping
    public ApiResult<List<Anime>> list() {
        return ApiResult.success(animeService.listPublished());
    }

    @GetMapping("/{id}")
    public ApiResult<Anime> detail(@PathVariable Long id) {
        return ApiResult.success(animeService.getById(id));
    }
}
