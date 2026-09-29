package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final AdminUserService users;

  public AdminUserController(AdminUserService users) {
    this.users = users;
  }

  @GetMapping
  @Operation(summary = "List every user")
  public ApiResponse<Page<UserResponse>> list(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success(users.list(search, PageRequest.of(page, size)));
  }

  @PostMapping
  @Operation(summary = "Create an Owner or User")
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.success(users.create(request));
  }

  @PatchMapping("/{id}/role")
  @Operation(summary = "Change a user's role")
  public ApiResponse<UserResponse> role(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
    return ApiResponse.success(users.changeRole(id, request));
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Activate or deactivate a user")
  public ApiResponse<UserResponse> status(
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserStatusRequest request,
      Authentication auth) {
    return ApiResponse.success(users.changeStatus(id, request, auth.getName()));
  }
}
