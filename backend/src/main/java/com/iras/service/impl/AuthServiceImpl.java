package com.iras.service.impl;

import com.iras.dto.LoginRequest;
import com.iras.dto.RegisterRequest;
import com.iras.dto.Result;
import com.iras.entity.User;
import com.iras.mapper.UserMapper;
import com.iras.service.AuthService;
import com.iras.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Result<Map<String, Object>> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            return Result.error(400, "用户名已存在");
        }

        // 检查邮箱是否已被使用
        if (request.getEmail() != null && userMapper.findByEmail(request.getEmail()) != null) {
            return Result.error(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());

        return Result.success("注册成功", data);
    }

    @Override
    public Result<Map<String, Object>> login(LoginRequest request) {
        // 支持用户名或邮箱登录
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.findByEmail(request.getUsername());
        }

        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());

        return Result.success("登录成功", data);
    }
}
