/**
 * @file DifyController.java
 * @description Dify AI 服务控制器。
 *              提供职能画像分析和简历智能诊断两个 AI 接口。
 *              将前端请求转发到 Dify AI 平台的 Workflow API。
 *
 * @author IRAS Team
 * @since 1.0
 */
package com.iras.controller;

import com.iras.dto.Result;
import com.iras.service.DifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Dify AI 服务控制器。
 * <p>
 * 提供两个 AI 增强接口：
 * <ul>
 *   <li>职能画像：输入职位名称，AI 生成该职位的技能要求、工具清单等完整画像</li>
 *   <li>智能诊断：上传简历文件，AI 进行岗位匹配和诊断分析</li>
 * </ul>
 * 两个接口的处理时间较长（约 2-3 分钟），需要设置较长的超时时间。
 * </p>
 */
@Slf4j                          // 启用 Lombok 日志
@RestController                  // 声明为 REST 控制器
@RequestMapping("/api/dify")     // 映射请求路径前缀
@RequiredArgsConstructor
public class DifyController {

    /** Dify 服务，封装与 Dify AI 平台的交互逻辑 */
    private final DifyService difyService;

    /**
     * 职能画像接口 - 接收职位名称，转发到 Dify AI 平台进行分析。
     * <p>
     * 请求体格式：{@code {"job_name": "软件工程师"}}
     * 返回该职位的完整画像信息（技能要求、工具、经验等）。
     * </p>
     *
     * @param request 请求体，包含 job_name 字段
     * @return 统一响应结果，包含 AI 生成的职能画像 JSON 字符串
     */
    @PostMapping("/job-profile")
    public Result<String> getJobProfile(@RequestBody Map<String, String> request) {
        // 从请求体中获取职位名称
        String jobName = request.get("job_name");
        // 校验职位名称是否为空
        if (jobName == null || jobName.isBlank()) {
            return Result.error(400, "职位名称不能为空");
        }
        try {
            log.info("调用职能画像 API, jobName={}", jobName);
            // 调用 Dify 服务获取职能画像
            String result = difyService.getJobProfile(jobName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("职能画像 API 调用失败", e);
            return Result.error(500, "AI 分析失败: " + e.getMessage());
        }
    }

    /**
     * 智能诊断接口 - 接收简历文件，转发到 Dify AI 平台进行诊断分析。
     * <p>
     * 使用 multipart/form-data 格式上传文件。
     * AI 将对简历进行深度分析，匹配最适合的岗位并生成诊断报告。
     * </p>
     *
     * @param file 上传的简历文件，支持 .txt/.pdf/.doc/.docx 格式
     * @return 统一响应结果，包含 AI 生成的诊断报告 JSON 字符串
     */
    @PostMapping("/diagnose")
    public Result<String> diagnoseResume(@RequestParam("file") MultipartFile file) {
        // 校验文件是否为空
        if (file.isEmpty()) {
            return Result.error(400, "请上传简历文件");
        }
        try {
            log.info("调用智能诊断 API, filename={}, size={}", file.getOriginalFilename(), file.getSize());
            // 调用 Dify 服务进行简历诊断
            String result = difyService.diagnoseResume(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("智能诊断 API 调用失败", e);
            return Result.error(500, "AI 诊断失败: " + e.getMessage());
        }
    }
}
