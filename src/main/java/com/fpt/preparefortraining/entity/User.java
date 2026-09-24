package com.fpt.preparefortraining.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void create() {
    createdAt = Instant.now();
    updatedAt = createdAt;
    if (status == null) status = UserStatus.ACTIVE;
  }

  @PreUpdate
  void update() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String v) {
    email = v;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String v) {
    passwordHash = v;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String v) {
    fullName = v;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role v) {
    role = v;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus v) {
    status = v;
  }
}
