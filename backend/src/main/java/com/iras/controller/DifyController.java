package com.iras.controller;

import com.iras.dto.Result;
import com.iras.service.DifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dify")
@RequiredArgsConstructor
public class DifyController {

    private final DifyService difyService;

    /**
     * 职能画像 - 接收职位名称，转发 Dify
     */
    @PostMapping("/job-profile")
    public Result<String> getJobProfile(@RequestBody Map<String, String> request) {
        String jobName = request.get("job_name");
        if (jobName == null || jobName.isBlank()) {
            return Result.error(400, "职位名称不能为空");
        }
        try {
            log.info("调用职能画像 API, jobName={}", jobName);
            String result = difyService.getJobProfile(jobName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("职能画像 API 调用失败", e);
            return Result.error(500, "AI 分析失败: " + e.getMessage());
        }
    }

    /**
     * 智能诊断 - 接收简历文件，转发 Dify
     */
    @PostMapping("/diagnose")
    public Result<String> diagnoseResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请上传简历文件");
        }
        try {
            log.info("调用智能诊断 API, filename={}, size={}", file.getOriginalFilename(), file.getSize());
            String result = difyService.diagnoseResume(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("智能诊断 API 调用失败", e);
            return Result.error(500, "AI 诊断失败: " + e.getMessage());
        }
    }
}
