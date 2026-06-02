package com.dumb.service;

import com.dumb.dto.request.AnimeAiGenerateRequest;
import com.dumb.dto.response.AnimeAiGenerateResponse;

public interface AnimeAiGenerationService {

    AnimeAiGenerateResponse generate(AnimeAiGenerateRequest request);
}
