package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.ReviewAiGenerateRequest;
import com.dumb.dto.request.ReviewCreateRequest;
import com.dumb.dto.request.ReviewSearchRequest;
import com.dumb.dto.response.ReviewAiGenerateResponse;
import com.dumb.dto.response.ReviewListItemVO;
import com.dumb.entity.Review;
import com.dumb.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController extends BaseController {
    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResult<Review> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResult.success(reviewService.create(request, currentUsername()));
    }

    @PostMapping("/ai-generate")
    public ApiResult<ReviewAiGenerateResponse> aiGenerate(@Valid @RequestBody ReviewAiGenerateRequest request) {
        return ApiResult.success(reviewService.generateAiAssist(request));
    }

    @GetMapping
    public ApiResult<List<ReviewListItemVO>> list(ReviewSearchRequest request) {
        return ApiResult.success(reviewService.adminSearch(request));
    }

    @GetMapping("/{id:\\d+}")
    public ApiResult<Review> detail(@PathVariable Long id) {
        return ApiResult.success(reviewService.adminGetById(id));
    }

    @PutMapping("/{id:\\d+}")
    public ApiResult<Review> update(@PathVariable Long id, @Valid @RequestBody ReviewCreateRequest request) {
        return ApiResult.success(reviewService.update(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        reviewService.deleteById(id);
        return ApiResult.success();
    }
}
