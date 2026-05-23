package com.symphonyforgeops.api.interfaces.web;

public record ApiResponse<T>(boolean success, T data, String message, String code) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "ok", null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, message, code);
    }
}

