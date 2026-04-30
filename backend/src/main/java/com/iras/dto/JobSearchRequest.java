package com.iras.dto;

import lombok.Data;

@Data
public class JobSearchRequest {
    private String jobName;
    private String city;
    private String salaryMin;
    private String salaryMax;
    private Integer page = 1;
    private Integer size = 20;
}
