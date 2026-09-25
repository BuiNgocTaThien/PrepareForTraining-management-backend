package com.fpt.preparefortraining.dto.response;

import com.fpt.preparefortraining.entity.Project;

public record ProjectResponse(
    Long id, String name, String description, Long ownerId, String ownerName, String status, boolean isPinned, boolean isStarred,
    long imageCount, long documentCount, long videoCount) {
  
  public static ProjectResponse from(Project p, boolean isStarred) {
    return from(p, isStarred, 0, 0, 0);
  }

  public static ProjectResponse from(Project p, boolean isStarred, long imageCount, long documentCount, long videoCount) {
    return new ProjectResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getOwner().getId(),
        p.getOwner().getFullName(),
        p.getStatus().name(),
        p.isPinned(),
        isStarred,
        imageCount,
        documentCount,
        videoCount);
  }

  public static ProjectResponse from(Project p) {
    return from(p, false);
  }
}
