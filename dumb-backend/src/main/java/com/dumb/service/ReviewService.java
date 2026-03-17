package com.dumb.service;

import com.dumb.dto.request.ReviewCreateRequest;
import com.dumb.dto.request.ReviewSearchRequest;
import com.dumb.dto.response.ReviewListItemVO;
import com.dumb.entity.Review;

import java.util.List;

public interface ReviewService {
    Review create(ReviewCreateRequest request, String username);
    List<ReviewListItemVO> search(ReviewSearchRequest request);
    List<ReviewListItemVO> adminSearch(ReviewSearchRequest request);
    Review getById(Long id);
    Review adminGetById(Long id);
    Review update(Long id, ReviewCreateRequest request);
    void deleteById(Long id);
}
