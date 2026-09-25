package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a standard user")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(auth.register(request)));
  }

  @PostMapping("/google")
  @Operation(summary = "Log in or register with Google")
  public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
    return ApiResponse.success(auth.googleLogin(request));
  }

  @PostMapping("/login")
  @Operation(summary = "Log in and receive a JWT")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success(auth.login(request));
  }

  @PostMapping("/forgot-password")
  @Operation(summary = "Request a password reset link")
  public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    auth.forgotPassword(request.email());
    return ApiResponse.success("If this email is registered, a password reset link has been sent.");
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset password using a valid token")
  public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    auth.resetPassword(request.token(), request.newPassword());
    return ApiResponse.success("Password has been successfully reset.");
  }
}
