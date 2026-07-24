package com.pinshengsheng.auth.controller;

import com.pinshengsheng.auth.dto.LoginRequest;
import com.pinshengsheng.auth.log.OperationLog;
import com.pinshengsheng.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // 记录用户登录操作
    @OperationLog("用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(
            @RequestBody LoginRequest loginRequest) {

        // 判断当前登录的是管理员还是普通用户
        boolean isAdmin = "admin".equals(loginRequest.getUsername());
        boolean isNormalUser = "user".equals(loginRequest.getUsername());

        // 密码错误，或用户名不属于两种测试账号时，登录失败
        if (!"123456".equals(loginRequest.getPassword())
                || (!isAdmin && !isNormalUser)) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        // 根据用户身份返回对应的临时 Token 和角色
        String token = isAdmin ? "temp-token-admin" : "temp-token-user";
        String role = isAdmin ? "ADMIN" : "USER";

        // 返回登录成功后的身份信息
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", loginRequest.getUsername());
        data.put("role", role);

        return ApiResponse.success(data);
    }
    @GetMapping("/profile")
    public ApiResponse<Map<String, String>> profile(
            @RequestHeader(value = "Authorization", required = false) String authorization){
        // 分别判断当前 Token 是否属于管理员或普通用户
        boolean isAdmin = "Bearer temp-token-admin".equals(authorization);
        boolean isNormalUser = "Bearer temp-token-user".equals(authorization);
        if(!isAdmin && !isNormalUser){
            return ApiResponse.fail(401, "登陆状态已失效");
        }
        // 根据 Token 区分当前用户身份
        String username = isAdmin ? "admin" : "user";
        String nickname = isAdmin ? "拼省省管理员" : "拼省省普通用户";
        String role = isAdmin ? "ADMIN" : "USER";

        // 返回当前登录用户的信息和角色
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("nickname", nickname);
        data.put("role", role);

        return ApiResponse.success(data);

    }
    // 模拟只有管理员才能访问的后台概览接口
    @OperationLog("查看管理员后台")
    @GetMapping("/admin/dashboard")
    public ApiResponse<Map<String, String>> adminDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 判断当前请求携带的是哪种 Token
        boolean isAdmin = "Bearer temp-token-admin".equals(authorization);
        boolean isNormalUser = "Bearer temp-token-user".equals(authorization);

        // 没有有效 Token，说明用户未登录
        if (!isAdmin && !isNormalUser) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        // 已登录但不是管理员，说明权限不足
        if (!isAdmin) {
            return ApiResponse.fail(403, "没有管理员权限");
        }

        // 管理员可以看到后台概览数据
        Map<String, String> data = new HashMap<>();
        data.put("message", "欢迎进入拼省省管理后台");
        data.put("role", "ADMIN");

        return ApiResponse.success(data);
    }
}
