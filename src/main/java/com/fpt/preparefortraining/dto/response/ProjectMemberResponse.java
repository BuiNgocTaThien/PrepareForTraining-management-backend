package com.fpt.preparefortraining.dto.response;

import com.fpt.preparefortraining.entity.ProjectMember;
import java.time.Instant;

public record ProjectMemberResponse(
    Long userId, String email, String fullName, String role, Instant joinedAt) {
  public static ProjectMemberResponse from(ProjectMember m) {
    return new ProjectMemberResponse(
        m.getUser().getId(),
        m.getUser().getEmail(),
        m.getUser().getFullName(),
        m.getUser().getRole().name(),
        m.getJoinedAt());
  }
}
