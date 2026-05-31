package com.dumb.service;

import com.dumb.dto.request.ReviewAiGenerateRequest;
import com.dumb.dto.response.ReviewAiGenerateResponse;

public interface ReviewAiGenerationService {

    ReviewAiGenerateResponse generate(ReviewAiGenerateRequest request);
}
