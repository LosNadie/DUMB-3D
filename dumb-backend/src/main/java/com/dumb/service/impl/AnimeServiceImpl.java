package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ContentStatusEnum;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.AnimeCreateRequest;
import com.dumb.entity.Anime;
import com.dumb.entity.User;
import com.dumb.mapper.AnimeMapper;
import com.dumb.mapper.UserMapper;
import com.dumb.service.AnimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnimeServiceImpl implements AnimeService {

    private final AnimeMapper animeMapper;
    private final UserMapper userMapper;

    public AnimeServiceImpl(AnimeMapper animeMapper, UserMapper userMapper) {
        this.animeMapper = animeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Anime create(AnimeCreateRequest request, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }
        Anime anime = new Anime();
        anime.setTitle(request.getTitle());
        anime.setContent(request.getContent());
        anime.setCoverImage(request.getCoverImage());
        anime.setAuthorId(user.getId());
        anime.setScore(request.getScore());
        anime.setStudio(request.getStudio());
        anime.setGenre(joinGenres(request.getGenres()));
        anime.setBackgroundImage(request.getBackgroundImage());
        anime.setReleaseDate(request.getReleaseDate());
        anime.setStatus(ContentStatusEnum.PUBLISHED.getCode());
        anime.setPublishTime(LocalDateTime.now());
        anime.setViews(0);
        animeMapper.insert(anime);
        return anime;
    }

    @Override
    public List<Anime> listPublished() {
        return animeMapper.selectList(new LambdaQueryWrapper<Anime>()
            .eq(Anime::getStatus, ContentStatusEnum.PUBLISHED.getCode())
            .orderByDesc(Anime::getPublishTime));
    }

    @Override
    public Anime getById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null || !ContentStatusEnum.PUBLISHED.getCode().equals(anime.getStatus())) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "动漫不存在");
        }
        anime.setViews(anime.getViews() + 1);
        animeMapper.updateById(anime);
        return anime;
    }

    @Override
    public List<Anime> adminList(String keyword) {
        LambdaQueryWrapper<Anime> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Anime::getTitle, keyword.trim())
                .or().like(Anime::getStudio, keyword.trim())
                .or().like(Anime::getContent, keyword.trim()));
        }
        return animeMapper.selectList(wrapper.orderByDesc(Anime::getId));
    }

    @Override
    public Anime adminGetById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "动漫不存在");
        }
        return anime;
    }

    @Override
    public Anime update(Long id, AnimeCreateRequest request) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "动漫不存在");
        }
        anime.setTitle(request.getTitle());
        anime.setContent(request.getContent());
        anime.setCoverImage(request.getCoverImage());
        anime.setScore(request.getScore());
        anime.setStudio(request.getStudio());
        anime.setGenre(joinGenres(request.getGenres()));
        anime.setBackgroundImage(request.getBackgroundImage());
        anime.setReleaseDate(request.getReleaseDate());
        anime.setUpdateTime(java.time.LocalDateTime.now());
        animeMapper.updateById(anime);
        return anime;
    }

    @Override
    public void deleteById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "动漫不存在");
        }
        animeMapper.deleteById(id);
    }

    private String joinGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }
        return genres.stream()
            .filter(item -> item != null && !item.trim().isEmpty())
            .map(String::trim)
            .collect(Collectors.joining(","));
    }
}
