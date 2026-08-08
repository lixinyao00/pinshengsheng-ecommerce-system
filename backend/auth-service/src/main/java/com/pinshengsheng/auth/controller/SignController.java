package com.pinshengsheng.auth.controller;

import com.pinshengsheng.auth.service.SignService;
import com.pinshengsheng.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/sign")
public class SignController {

    private final SignService signService;

    public SignController(SignService signService) {
        this.signService = signService;
    }

    @PostMapping("/{userId}/day/{day}")
    public ApiResponse<Map<String, Object>> sign(
            @PathVariable Long userId,
            @PathVariable Integer day) {

        boolean firstSign = signService.sign(userId, day);

        if (!firstSign) {
            return ApiResponse.fail(409, "今天已经签到过了");
        }

        return ApiResponse.success(Map.of(
                "userId", userId,
                "day", day,
                "signed", true
        ));
    }

    @GetMapping("/{userId}/day/{day}")
    public ApiResponse<Map<String, Object>> signed(
            @PathVariable Long userId,
            @PathVariable Integer day) {

        return ApiResponse.success(Map.of(
                "userId", userId,
                "day", day,
                "signed", signService.signed(userId, day)
        ));
    }

    @GetMapping("/{userId}/count")
    public ApiResponse<Map<String, Object>> signCount(
            @PathVariable Long userId) {

        return ApiResponse.success(Map.of(
                "userId", userId,
                "signCount", signService.signCount(userId)
        ));
    }
}
