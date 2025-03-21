package com.example.labdata.common;

import java.time.LocalDateTime;

/**
 * 通用API响应
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    // 无参构造函数
    public ApiResponse() {
    }

    // 构造函数 - 成功/失败和消息
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 构造函数 - 成功/失败、消息和数据
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 静态方法 - 创建成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    // 静态方法 - 创建成功响应（带自定义消息）
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    // 静态方法 - 创建失败响应
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    // 静态方法 - 创建失败响应（带数据）
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
