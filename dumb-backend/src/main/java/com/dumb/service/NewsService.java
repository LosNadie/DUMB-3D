package com.dumb.service;

import com.dumb.dto.request.NewsCreateRequest;
import com.dumb.entity.News;

import java.util.List;

public interface NewsService {
    News create(NewsCreateRequest request, String username);
    List<News> listPublished();
    News getById(Long id);
    List<News> adminList(String keyword);
    News adminGetById(Long id);
    News update(Long id, NewsCreateRequest request);
    void deleteById(Long id);
}
