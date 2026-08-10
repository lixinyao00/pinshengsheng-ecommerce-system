package com.pinshengsheng.auth.controller;

import com.pinshengsheng.auth.dto.LoginRequest;
import com.pinshengsheng.auth.dto.RegisterRequest;
import com.pinshengsheng.auth.log.OperationLog;
import com.pinshengsheng.auth.model.UserAccount;
import com.pinshengsheng.auth.service.UserService;
import com.pinshengsheng.common.auth.TokenUtils;
import com.pinshengsheng.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @OperationLog("用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(
            @RequestBody LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.isBlank() || password == null) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        // 保留项目演示用的管理员账号
        if ("admin".equals(username)) {
            if (!"123456".equals(password)) {
                return ApiResponse.fail(401, "用户名或密码错误");
            }
            return ApiResponse.success(buildLoginData(
                    "admin", "拼省省管理员", "ADMIN", "temp-token-admin"));
        }

        // 保留原来的普通用户演示账号，避免影响已有测试数据
        if ("user".equals(username) && "123456".equals(password)) {
            return ApiResponse.success(buildLoginData(
                    "user", "拼省省普通用户", "USER", "temp-token-user"));
        }

        UserAccount account = userService.findByUsername(username);
        if (account == null
                || !Integer.valueOf(1).equals(account.getStatus())
                || !userService.matchesPassword(password, account.getPasswordHash())) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        String token = TokenUtils.buildUserToken(account.getId());
        return ApiResponse.success(buildLoginData(
                account.getUsername(), account.getNickname(), account.getRole(), token));
    }

    @OperationLog("用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody RegisterRequest request) {
        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().length() < 6) {
            return ApiResponse.fail(400, "用户名不能为空，密码至少 6 位");
        }

        if ("admin".equalsIgnoreCase(request.getUsername())
                || "user".equalsIgnoreCase(request.getUsername())) {
            return ApiResponse.fail(409, "该用户名不能注册");
        }

        if (request.getNickname() == null || request.getNickname().isBlank()) {
            request.setNickname(request.getUsername());
        }

        UserAccount account = userService.register(request);
        if (account == null) {
            return ApiResponse.fail(409, "用户名已存在");
        }

        Map<String, String> data = new HashMap<>();
        data.put("username", account.getUsername());
        data.put("nickname", account.getNickname());
        return ApiResponse.success(data);
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, String>> profile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (TokenUtils.isAdminToken(authorization)) {
            return ApiResponse.success(buildProfileData(
                    "admin", "拼省省管理员", "ADMIN"));
        }

        if ("Bearer temp-token-user".equals(authorization)) {
            return ApiResponse.success(buildProfileData(
                    "user", "拼省省普通用户", "USER"));
        }

        Long userId = TokenUtils.getUserId(authorization);
        UserAccount account = userId == null ? null : userService.findById(userId);
        if (account == null || !Integer.valueOf(1).equals(account.getStatus())) {
            return ApiResponse.fail(401, "登陆状态已失效");
        }

        return ApiResponse.success(buildProfileData(
                account.getUsername(), account.getNickname(), account.getRole()));
    }

    @OperationLog("查看管理员后台")
    @GetMapping("/admin/dashboard")
    public ApiResponse<Map<String, String>> adminDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!TokenUtils.isAdminToken(authorization)
                && !TokenUtils.isUserToken(authorization)) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        if (!TokenUtils.isAdminToken(authorization)) {
            return ApiResponse.fail(403, "没有管理员权限");
        }

        Map<String, String> data = new HashMap<>();
        data.put("message", "欢迎进入拼省省管理后台");
        data.put("role", "ADMIN");
        return ApiResponse.success(data);
    }

    private Map<String, String> buildLoginData(
            String username,
            String nickname,
            String role,
            String token) {
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        data.put("nickname", nickname);
        data.put("role", role);
        return data;
    }

    private Map<String, String> buildProfileData(
            String username,
            String nickname,
            String role) {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("nickname", nickname);
        data.put("role", role);
        return data;
    }
}
