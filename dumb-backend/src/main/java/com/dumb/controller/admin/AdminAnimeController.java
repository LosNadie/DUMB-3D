package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.AnimeCreateRequest;
import com.dumb.entity.Anime;
import com.dumb.service.AnimeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/anime")
public class AdminAnimeController extends BaseController {
    private final AnimeService animeService;

    public AdminAnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @PostMapping
    public ApiResult<Anime> create(@Valid @RequestBody AnimeCreateRequest request) {
        return ApiResult.success(animeService.create(request, currentUsername()));
    }

    @GetMapping
    public ApiResult<List<Anime>> list(@RequestParam(required = false) String keyword) {
        return ApiResult.success(animeService.adminList(keyword));
    }

    @GetMapping("/{id}")
    public ApiResult<Anime> getById(@PathVariable Long id) {
        return ApiResult.success(animeService.adminGetById(id));
    }

    @PutMapping("/{id}")
    public ApiResult<Anime> update(@PathVariable Long id, @Valid @RequestBody AnimeCreateRequest request) {
        return ApiResult.success(animeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        animeService.deleteById(id);
        return ApiResult.success(null);
    }
}
