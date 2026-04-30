package com.iras.service;

public interface DifyService {
    /**
     * 调用 Dify 职能画像 API
     */
    String getJobProfile(String jobName);

    /**
     * 调用 Dify 简历诊断 API
     */
    String diagnoseResume(String resumeText);
}
