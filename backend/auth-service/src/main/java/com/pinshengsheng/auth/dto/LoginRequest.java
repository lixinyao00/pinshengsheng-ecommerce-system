package com.pinshengsheng.auth.dto;

// 接收前端提交的登录参数
public class LoginRequest {

    private String username;
    private String password;

    // Spring 接收 JSON 后会通过 setter 给字段赋值
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}