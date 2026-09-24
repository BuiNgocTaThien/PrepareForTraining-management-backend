package com.fpt.preparefortraining.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "project_stars")
public class ProjectStar {
  @EmbeddedId
  private ProjectStarId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("projectId")
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void create() {
    createdAt = Instant.now();
  }

  public ProjectStarId getId() {
    return id;
  }

  public void setId(ProjectStarId id) {
    this.id = id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
