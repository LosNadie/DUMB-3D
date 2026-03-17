package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.NewsCreateRequest;
import com.dumb.entity.News;
import com.dumb.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/news")
public class AdminNewsController extends BaseController {
    private final NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    public ApiResult<News> create(@Valid @RequestBody NewsCreateRequest request) {
        return ApiResult.success(newsService.create(request, currentUsername()));
    }

    @GetMapping
    public ApiResult<List<News>> list(@RequestParam(required = false) String keyword) {
        return ApiResult.success(newsService.adminList(keyword));
    }

    @GetMapping("/{id}")
    public ApiResult<News> getById(@PathVariable Long id) {
        return ApiResult.success(newsService.adminGetById(id));
    }

    @PutMapping("/{id}")
    public ApiResult<News> update(@PathVariable Long id, @Valid @RequestBody NewsCreateRequest request) {
        return ApiResult.success(newsService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        newsService.deleteById(id);
        return ApiResult.success(null);
    }
}
