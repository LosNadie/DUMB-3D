package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.dto.request.ReviewSearchRequest;
import com.dumb.dto.response.ReviewListItemVO;
import com.dumb.entity.Review;
import com.dumb.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ApiResult<List<ReviewListItemVO>> search(ReviewSearchRequest request) {
        return ApiResult.success(reviewService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResult<Review> detail(@PathVariable Long id) {
        return ApiResult.success(reviewService.getById(id));
    }
}
