package com.iras.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iras.service.DifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {

    @Value("${dify.base-url}")
    private String baseUrl;

    @Value("${dify.job-profile-key}")
    private String jobProfileKey;

    @Value("${dify.resume-diagnosis-key}")
    private String resumeDiagnosisKey;

    private final ObjectMapper objectMapper;

    @Override
    public String getJobProfile(String jobName) {
        String url = baseUrl + "/workflows/run";
        String requestBody = buildWorkflowRequest("job_name", jobName);
        return callDifyApi(url, requestBody, jobProfileKey);
    }

    @Override
    public String diagnoseResume(String resumeText) {
        String url = baseUrl + "/workflows/run";
        String requestBody = buildWorkflowRequest("resume_text", resumeText);
        return callDifyApi(url, requestBody, resumeDiagnosisKey);
    }

    private String buildWorkflowRequest(String inputKey, String inputValue) {
        try {
            return objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("inputs", new java.util.HashMap<>() {{
                    put(inputKey, inputValue);
                }});
                put("response_mode", "blocking");
                put("user", "iras-user");
            }});
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            throw new RuntimeException("构建请求体失败", e);
        }
    }

    private String callDifyApi(String urlStr, String requestBody, String apiKey) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(300000); // 5分钟超时（Dify推理约3分钟）
            connection.setReadTimeout(300000);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            java.io.InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Dify API 响应: code={}, body={}", responseCode, responseBody);

            if (responseCode >= 200 && responseCode < 300) {
                // 解析 Dify 响应，提取 outputs.result
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode outputs = root.path("data").path("outputs");
                if (outputs.has("result")) {
                    return outputs.get("result").asText();
                }
                return responseBody;
            } else {
                throw new RuntimeException("Dify API 调用失败: " + responseCode + " - " + responseBody);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Dify API 异常", e);
            throw new RuntimeException("调用 Dify API 失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
