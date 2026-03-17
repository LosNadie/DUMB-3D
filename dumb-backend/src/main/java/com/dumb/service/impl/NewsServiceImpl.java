package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ContentStatusEnum;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.NewsCreateRequest;
import com.dumb.entity.News;
import com.dumb.entity.User;
import com.dumb.mapper.NewsMapper;
import com.dumb.mapper.UserMapper;
import com.dumb.service.NewsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;
    private final UserMapper userMapper;

    public NewsServiceImpl(NewsMapper newsMapper, UserMapper userMapper) {
        this.newsMapper = newsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public News create(NewsCreateRequest request, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }
        News news = new News();
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setCoverImage(request.getCoverImage());
        news.setAuthorId(user.getId());
        news.setCategory(request.getCategory());
        news.setStatus(ContentStatusEnum.PUBLISHED.getCode());
        news.setPublishTime(LocalDateTime.now());
        news.setViews(0);
        newsMapper.insert(news);
        return news;
    }

    @Override
    public List<News> listPublished() {
        return newsMapper.selectList(new LambdaQueryWrapper<News>()
            .eq(News::getStatus, ContentStatusEnum.PUBLISHED.getCode())
            .orderByDesc(News::getPublishTime));
    }

    @Override
    public News getById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null || !ContentStatusEnum.PUBLISHED.getCode().equals(news.getStatus())) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "新闻不存在");
        }
        news.setViews(news.getViews() + 1);
        newsMapper.updateById(news);
        return news;
    }

    @Override
    public List<News> adminList(String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(News::getTitle, keyword.trim())
                .or().like(News::getContent, keyword.trim()));
        }
        return newsMapper.selectList(wrapper.orderByDesc(News::getId));
    }

    @Override
    public News adminGetById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "新闻不存在");
        }
        return news;
    }

    @Override
    public News update(Long id, NewsCreateRequest request) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "新闻不存在");
        }
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setCoverImage(request.getCoverImage());
        news.setCategory(request.getCategory());
        newsMapper.updateById(news);
        return news;
    }

    @Override
    public void deleteById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "新闻不存在");
        }
        newsMapper.deleteById(id);
    }
}
