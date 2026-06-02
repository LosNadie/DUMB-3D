package com.dumb.service;

import com.dumb.dto.request.MovieAiGenerateRequest;
import com.dumb.dto.response.MovieAiGenerateResponse;

public interface MovieAiGenerationService {

    MovieAiGenerateResponse generate(MovieAiGenerateRequest request);
}
