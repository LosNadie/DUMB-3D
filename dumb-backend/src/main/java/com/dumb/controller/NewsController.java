package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.entity.News;
import com.dumb.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ApiResult<List<News>> list() {
        return ApiResult.success(newsService.listPublished());
    }

    @GetMapping("/{id}")
    public ApiResult<News> detail(@PathVariable Long id) {
        return ApiResult.success(newsService.getById(id));
    }
}
