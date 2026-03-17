package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ContentStatusEnum;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.InterviewCreateRequest;
import com.dumb.entity.Interview;
import com.dumb.entity.User;
import com.dumb.mapper.InterviewMapper;
import com.dumb.mapper.UserMapper;
import com.dumb.service.InterviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {
    private final InterviewMapper interviewMapper;
    private final UserMapper userMapper;

    public InterviewServiceImpl(InterviewMapper interviewMapper, UserMapper userMapper) {
        this.interviewMapper = interviewMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Interview create(InterviewCreateRequest request, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }
        Interview interview = new Interview();
        interview.setArtistName(request.getArtistName());
        interview.setTitle(request.getTitle());
        interview.setContent(request.getContent());
        interview.setCoverImage(request.getCoverImage());
        interview.setAuthorId(user.getId());
        interview.setStatus(ContentStatusEnum.PUBLISHED.getCode());
        interview.setPublishTime(LocalDateTime.now());
        interviewMapper.insert(interview);
        return interview;
    }

    @Override
    public List<Interview> listPublished() {
        return interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
            .eq(Interview::getStatus, ContentStatusEnum.PUBLISHED.getCode())
            .orderByDesc(Interview::getPublishTime));
    }

    @Override
    public Interview getById(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null || !ContentStatusEnum.PUBLISHED.getCode().equals(interview.getStatus())) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "访谈不存在");
        }
        return interview;
    }

    @Override
    public List<Interview> adminList(String keyword) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Interview::getTitle, keyword.trim())
                .or().like(Interview::getArtistName, keyword.trim())
                .or().like(Interview::getContent, keyword.trim()));
        }
        return interviewMapper.selectList(wrapper.orderByDesc(Interview::getId));
    }

    @Override
    public Interview adminGetById(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "访谈不存在");
        }
        return interview;
    }

    @Override
    public Interview update(Long id, InterviewCreateRequest request) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "访谈不存在");
        }
        interview.setArtistName(request.getArtistName());
        interview.setTitle(request.getTitle());
        interview.setContent(request.getContent());
        interview.setCoverImage(request.getCoverImage());
        interviewMapper.updateById(interview);
        return interview;
    }

    @Override
    public void deleteById(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "访谈不存在");
        }
        interviewMapper.deleteById(id);
    }
}
