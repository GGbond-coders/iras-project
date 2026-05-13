/**
 * @file AdminController.java
 * @description 管理员控制器。
 *              提供管理后台的 REST API，包括用户管理、职位管理和系统统计。
 *              所有接口需要管理员角色才能访问。
 *
 * @author IRAS Team
 * @since 1.0
 */
package com.iras.controller;

import com.iras.dto.PageResult;
import com.iras.dto.Result;
import com.iras.entity.JobInfo;
import com.iras.entity.User;
import com.iras.mapper.UserMapper;
import com.iras.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper;

    /**
     * 校验当前用户是否为管理员，非管理员返回 null。
     */
    private User requireAdmin(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userMapper.findByUsername(username);
        if (user == null || !"admin".equals(user.getRole())) {
            return null;
        }
        return user;
    }

    // ==================== 用户管理 ====================

    /** 获取用户列表（分页）- 仅管理员 */
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        return Result.success(adminService.getUsers(page, size));
    }

    /** 修改用户角色 - 仅管理员 */
    @PutMapping("/users/{id}/role")
    public Result<String> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                         Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        String role = body.get("role");
        if (role == null || (!role.equals("user") && !role.equals("admin"))) {
            return Result.error(400, "角色值无效，仅支持 user 或 admin");
        }
        if (adminService.updateUserRole(id, role)) {
            return Result.success("修改成功", null);
        }
        return Result.error(400, "修改失败");
    }

    /** 删除用户 - 仅管理员 */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        User admin = requireAdmin(authentication);
        if (admin == null) return Result.error(403, "无权限");
        // 防止管理员删除自己
        if (admin.getId().equals(id)) {
            return Result.error(400, "不能删除当前登录用户");
        }
        if (adminService.deleteUser(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error(400, "删除失败");
    }

    // ==================== 职位管理 ====================

    /** 获取职位列表（分页）- 仅管理员 */
    @GetMapping("/jobs")
    public Result<PageResult<JobInfo>> getJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        return Result.success(adminService.getJobs(page, size));
    }

    /** 新增职位 - 仅管理员 */
    @PostMapping("/jobs")
    public Result<String> addJob(@RequestBody JobInfo job, Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        adminService.addJob(job);
        return Result.success("添加成功", null);
    }

    /** 更新职位 - 仅管理员 */
    @PutMapping("/jobs/{id}")
    public Result<String> updateJob(@PathVariable Long id, @RequestBody JobInfo job,
                                    Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        job.setId(id);
        if (adminService.updateJob(job)) {
            return Result.success("更新成功", null);
        }
        return Result.error(400, "更新失败");
    }

    /** 删除职位 - 仅管理员 */
    @DeleteMapping("/jobs/{id}")
    public Result<String> deleteJob(@PathVariable Long id, Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        if (adminService.deleteJob(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error(400, "删除失败");
    }

    // ==================== 系统统计 ====================

    /** 获取系统统计数据 - 仅管理员 */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(Authentication authentication) {
        if (requireAdmin(authentication) == null) return Result.error(403, "无权限");
        return Result.success(adminService.getStatistics());
    }
}
