package com.pinshengsheng.common.api;

// 所有接口统一使用这个结构返回，前端只需要按 code、message、data 处理结果
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public static <T> ApiResponse<T> success(T data) {
        // 成功时保留具体业务数据
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        // 失败时 data 为空，错误原因放在 message 中
        return new ApiResponse<>(code, message, null);
    }
}
