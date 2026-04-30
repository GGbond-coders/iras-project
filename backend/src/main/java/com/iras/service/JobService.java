package com.iras.service;

import com.iras.dto.JobSearchRequest;
import com.iras.dto.PageResult;
import com.iras.entity.JobInfo;

public interface JobService {
    PageResult<JobInfo> searchJobs(JobSearchRequest request);
    JobInfo getJobById(Long id);
}
