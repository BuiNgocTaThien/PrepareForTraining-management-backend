package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final AuthService auth;

  public UserController(AuthService auth) {
    this.auth = auth;
  }

  @GetMapping("/me")
  @Operation(summary = "Get profile of authenticated user")
  public ApiResponse<UserResponse> me(Authentication authentication) {
    return ApiResponse.success(auth.currentUser(authentication.getName()));
  }

  @PutMapping("/me/profile")
  @Operation(summary = "Update profile of authenticated user")
  public ApiResponse<UserResponse> updateProfile(
      Authentication authentication,
      @jakarta.validation.Valid @RequestBody com.fpt.preparefortraining.dto.request.UpdateProfileRequest request) {
    return ApiResponse.success(auth.updateProfile(authentication.getName(), request));
  }

  @PutMapping("/me/password")
  @Operation(summary = "Change password of authenticated user")
  public ApiResponse<String> changePassword(
      Authentication authentication,
      @jakarta.validation.Valid @RequestBody com.fpt.preparefortraining.dto.request.ChangePasswordRequest request) {
    auth.changePassword(authentication.getName(), request);
    return ApiResponse.success("Đổi mật khẩu thành công");
  }
}
