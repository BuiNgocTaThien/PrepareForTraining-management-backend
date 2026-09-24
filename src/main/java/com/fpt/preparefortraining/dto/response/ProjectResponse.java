package com.fpt.preparefortraining.dto.response;

import com.fpt.preparefortraining.entity.Project;

public record ProjectResponse(
    Long id, String name, String description, Long ownerId, String ownerName, String status, boolean isPinned, boolean isStarred) {
  
  public static ProjectResponse from(Project p, boolean isStarred) {
    return new ProjectResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getOwner().getId(),
        p.getOwner().getFullName(),
        p.getStatus().name(),
        p.isPinned(),
        isStarred);
  }

  public static ProjectResponse from(Project p) {
    return from(p, false);
  }
}
