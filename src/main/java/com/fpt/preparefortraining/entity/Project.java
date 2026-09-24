package com.fpt.preparefortraining.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "projects")
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "is_pinned", nullable = false)
  private boolean isPinned = false;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void create() {
    createdAt = Instant.now();
    updatedAt = createdAt;
    if (status == null) status = ProjectStatus.ACTIVE;
  }

  @PreUpdate
  void update() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User v) {
    owner = v;
  }

  public ProjectStatus getStatus() {
    return status;
  }

  public void setStatus(ProjectStatus v) {
    status = v;
  }

  public boolean isPinned() {
    return isPinned;
  }

  public void setPinned(boolean pinned) {
    this.isPinned = pinned;
  }
}
