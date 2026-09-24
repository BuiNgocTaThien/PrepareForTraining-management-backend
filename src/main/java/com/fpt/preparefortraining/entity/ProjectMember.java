package com.fpt.preparefortraining.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @PrePersist
  void create() {
    joinedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project v) {
    project = v;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User v) {
    user = v;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }
}
