package com.fpt.preparefortraining.dto.response;

import com.fpt.preparefortraining.entity.User;

public record UserResponse(Long id, String email, String fullName, String role, String status) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole().name(),
        user.getStatus().name());
  }
}
