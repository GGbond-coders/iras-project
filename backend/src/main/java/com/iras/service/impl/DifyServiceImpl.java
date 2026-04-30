package com.iras.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iras.service.DifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public String diagnoseResume(MultipartFile file) {
        // 1. 先上传文件到 Dify
        String uploadFileId = uploadFile(file);

        // 2. 用文件 ID 调用 workflow
        String url = baseUrl + "/workflows/run";
        String requestBody = buildFileWorkflowRequest("resume_text", uploadFileId);
        return callDifyApi(url, requestBody, resumeDiagnosisKey);
    }

    /**
     * 上传文件到 Dify，返回 upload_file_id
     */
    private String uploadFile(MultipartFile file) {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String uploadUrl = baseUrl + "/files/upload";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + resumeDiagnosisKey);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);

            try (OutputStream os = connection.getOutputStream()) {
                // file 字段
                os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" +
                        file.getOriginalFilename() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                os.write(("Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                os.write(file.getBytes());
                os.write("\r\n".getBytes(StandardCharsets.UTF_8));

                // user 字段
                os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                os.write("Content-Disposition: form-data; name=\"user\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                os.write("iras-user\r\n".getBytes(StandardCharsets.UTF_8));

                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            java.io.InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Dify 文件上传响应: code={}, body={}", responseCode, responseBody);

            if (responseCode >= 200 && responseCode < 300) {
                JsonNode root = objectMapper.readTree(responseBody);
                return root.get("id").asText();
            } else {
                throw new RuntimeException("文件上传失败: " + responseCode + " - " + responseBody);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件到 Dify 异常", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) connection.disconnect();
        }
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

    /**
     * 构建带文件引用的 workflow 请求体
     */
    private String buildFileWorkflowRequest(String inputKey, String uploadFileId) {
        try {
            return objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("inputs", new java.util.HashMap<>() {{
                    put(inputKey, java.util.List.of(new java.util.HashMap<>() {{
                        put("transfer_method", "local_file");
                        put("upload_file_id", uploadFileId);
                        put("type", "document");
                    }}));
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
            connection.setConnectTimeout(300000);
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
            if (connection != null) connection.disconnect();
        }
    }
}
