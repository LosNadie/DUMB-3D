package com.dumb.service;

import com.dumb.dto.request.InterviewCreateRequest;
import com.dumb.entity.Interview;

import java.util.List;

public interface InterviewService {
    Interview create(InterviewCreateRequest request, String username);
    List<Interview> listPublished();
    Interview getById(Long id);
    List<Interview> adminList(String keyword);
    Interview adminGetById(Long id);
    Interview update(Long id, InterviewCreateRequest request);
    void deleteById(Long id);
}
