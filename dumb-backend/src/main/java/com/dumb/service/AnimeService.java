package com.dumb.service;

import com.dumb.dto.request.AnimeCreateRequest;
import com.dumb.entity.Anime;

import java.util.List;

public interface AnimeService {
    Anime create(AnimeCreateRequest request, String username);
    List<Anime> listPublished();
    Anime getById(Long id);
    List<Anime> adminList(String keyword);
    Anime adminGetById(Long id);
    Anime update(Long id, AnimeCreateRequest request);
    void deleteById(Long id);
}
