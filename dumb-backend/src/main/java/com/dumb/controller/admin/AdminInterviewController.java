package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.InterviewCreateRequest;
import com.dumb.entity.Interview;
import com.dumb.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/interviews")
public class AdminInterviewController extends BaseController {
    private final InterviewService interviewService;

    public AdminInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ApiResult<Interview> create(@Valid @RequestBody InterviewCreateRequest request) {
        return ApiResult.success(interviewService.create(request, currentUsername()));
    }

    @GetMapping
    public ApiResult<List<Interview>> list(@RequestParam(required = false) String keyword) {
        return ApiResult.success(interviewService.adminList(keyword));
    }

    @GetMapping("/{id}")
    public ApiResult<Interview> getById(@PathVariable Long id) {
        return ApiResult.success(interviewService.adminGetById(id));
    }

    @PutMapping("/{id}")
    public ApiResult<Interview> update(@PathVariable Long id, @Valid @RequestBody InterviewCreateRequest request) {
        return ApiResult.success(interviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        interviewService.deleteById(id);
        return ApiResult.success(null);
    }
}
