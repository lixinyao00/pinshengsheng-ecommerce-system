package com.pinshengsheng.common.auth;

// 当前项目使用简单 Token，先统一 Token 格式，方便各服务识别用户编号
public final class TokenUtils {

    private static final String USER_TOKEN_PREFIX = "temp-token-user-";

    private TokenUtils() {
    }

    public static String buildUserToken(Long userId) {
        return USER_TOKEN_PREFIX + userId;
    }

    public static Long getUserId(String authorization) {
        if ("Bearer temp-token-user".equals(authorization)) {
            return 1L;
        }

        String prefix = "Bearer " + USER_TOKEN_PREFIX;
        if (authorization == null || !authorization.startsWith(prefix)) {
            return null;
        }

        try {
            return Long.valueOf(authorization.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static boolean isUserToken(String authorization) {
        return getUserId(authorization) != null;
    }

    public static boolean isAdminToken(String authorization) {
        return "Bearer temp-token-admin".equals(authorization);
    }
}
