package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ContentStatusEnum;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.MovieCreateRequest;
import com.dumb.entity.Movie;
import com.dumb.entity.User;
import com.dumb.mapper.MovieMapper;
import com.dumb.mapper.UserMapper;
import com.dumb.service.MovieService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieMapper movieMapper;
    private final UserMapper userMapper;

    public MovieServiceImpl(MovieMapper movieMapper, UserMapper userMapper) {
        this.movieMapper = movieMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Movie create(MovieCreateRequest request, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setContent(request.getContent());
        movie.setCoverImage(request.getCoverImage());
        movie.setAuthorId(user.getId());
        movie.setScore(request.getScore());
        movie.setDirector(request.getDirector());
        movie.setActors(request.getActors());
        movie.setGenre(joinGenres(request.getGenres()));
        movie.setRegion(request.getRegion());
        movie.setBackgroundImage(request.getBackgroundImage());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(ContentStatusEnum.PUBLISHED.getCode());
        movie.setPublishTime(LocalDateTime.now());
        movie.setViews(0);
        movieMapper.insert(movie);
        return movie;
    }

    @Override
    public List<Movie> listPublished() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
            .eq(Movie::getStatus, ContentStatusEnum.PUBLISHED.getCode())
            .orderByDesc(Movie::getPublishTime));
    }

    @Override
    public Movie getById(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null || !ContentStatusEnum.PUBLISHED.getCode().equals(movie.getStatus())) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "电影不存在");
        }
        movie.setViews(movie.getViews() + 1);
        movieMapper.updateById(movie);
        return movie;
    }

    @Override
    public List<Movie> adminList(String keyword) {
        LambdaQueryWrapper<Movie> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Movie::getTitle, keyword.trim())
                .or().like(Movie::getDirector, keyword.trim())
                .or().like(Movie::getActors, keyword.trim())
                .or().like(Movie::getContent, keyword.trim()));
        }
        return movieMapper.selectList(wrapper.orderByDesc(Movie::getId));
    }

    @Override
    public Movie adminGetById(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "电影不存在");
        }
        return movie;
    }

    @Override
    public Movie update(Long id, MovieCreateRequest request) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "电影不存在");
        }
        movie.setTitle(request.getTitle());
        movie.setContent(request.getContent());
        movie.setCoverImage(request.getCoverImage());
        movie.setScore(request.getScore());
        movie.setDirector(request.getDirector());
        movie.setActors(request.getActors());
        movie.setGenre(joinGenres(request.getGenres()));
        movie.setRegion(request.getRegion());
        movie.setBackgroundImage(request.getBackgroundImage());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setUpdateTime(java.time.LocalDateTime.now());
        movieMapper.updateById(movie);
        return movie;
    }

    @Override
    public void deleteById(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "电影不存在");
        }
        movieMapper.deleteById(id);
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
