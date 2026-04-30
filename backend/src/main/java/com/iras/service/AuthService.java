package com.iras.service;

import com.iras.dto.LoginRequest;
import com.iras.dto.RegisterRequest;
import com.iras.dto.Result;

import java.util.Map;

public interface AuthService {
    Result<Map<String, Object>> register(RegisterRequest request);
    Result<Map<String, Object>> login(LoginRequest request);
}
