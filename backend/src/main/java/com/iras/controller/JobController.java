package com.iras.controller;

import com.iras.dto.JobSearchRequest;
import com.iras.dto.PageResult;
import com.iras.dto.Result;
import com.iras.entity.JobInfo;
import com.iras.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 职位搜索 - 多字段模糊匹配 + 分页
     */
    @GetMapping("/search")
    public Result<PageResult<JobInfo>> searchJobs(JobSearchRequest request) {
        PageResult<JobInfo> result = jobService.searchJobs(request);
        return Result.success(result);
    }

    /**
     * 获取职位详情
     */
    @GetMapping("/{id}")
    public Result<JobInfo> getJobById(@PathVariable Long id) {
        JobInfo job = jobService.getJobById(id);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        return Result.success(job);
    }
}
