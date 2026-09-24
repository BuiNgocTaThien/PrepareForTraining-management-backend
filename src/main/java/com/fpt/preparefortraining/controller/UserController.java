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
}
