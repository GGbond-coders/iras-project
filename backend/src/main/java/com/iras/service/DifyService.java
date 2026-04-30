package com.iras.service;

import org.springframework.web.multipart.MultipartFile;

public interface DifyService {
    /**
     * 调用 Dify 职能画像 API
     */
    String getJobProfile(String jobName);

    /**
     * 调用 Dify 简历诊断 API（文件上传）
     */
    String diagnoseResume(MultipartFile file);
}
