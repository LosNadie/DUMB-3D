package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.entity.Interview;
import com.dumb.service.InterviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public ApiResult<List<Interview>> list() {
        return ApiResult.success(interviewService.listPublished());
    }

    @GetMapping("/{id}")
    public ApiResult<Interview> detail(@PathVariable Long id) {
        return ApiResult.success(interviewService.getById(id));
    }
}
