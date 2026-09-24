package com.fpt.preparefortraining.dto.response;

public record ApiResponse<T>(boolean success, String message, T data) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data);
  }

  public static <T> ApiResponse<T> failure(String message) {
    return new ApiResponse<>(false, message, null);
  }
}
