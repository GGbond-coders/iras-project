package com.iras.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobInfo {
    private Long id;
    private String jobName;
    private String companyName;
    private String city;
    private String salary;
    private String jdText;
    private String type;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
