package com.iras.service.impl;

import com.iras.dto.JobSearchRequest;
import com.iras.dto.PageResult;
import com.iras.entity.JobInfo;
import com.iras.mapper.JobMapper;
import com.iras.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;

    @Override
    public PageResult<JobInfo> searchJobs(JobSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 20;
        int offset = (page - 1) * size;

        List<JobInfo> records = jobMapper.searchJobs(
                request.getJobName(), request.getCity(),
                request.getSalaryMin(), request.getSalaryMax(),
                offset, size);

        long total = jobMapper.countSearchJobs(
                request.getJobName(), request.getCity(),
                request.getSalaryMin(), request.getSalaryMax());

        PageResult<JobInfo> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotalPages((int) Math.ceil((double) total / size));

        return result;
    }

    @Override
    public JobInfo getJobById(Long id) {
        return jobMapper.findById(id);
    }
}
