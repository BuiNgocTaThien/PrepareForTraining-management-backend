package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.UserResponse;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AdminUserService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  public Page<UserResponse> list(Pageable pageable) {
    return users.findAll(pageable).map(UserResponse::from);
  }

  @Transactional
  public UserResponse create(CreateUserRequest request) {
    String email = request.email().trim().toLowerCase();
    if (users.existsByEmail(email)) throw new BadRequestException("Email is already registered");
    User user = new User();
    user.setEmail(email);
    user.setFullName(request.fullName().trim());
    user.setPasswordHash(encoder.encode(request.password()));
    user.setRole(request.role());
    user.setStatus(UserStatus.ACTIVE);
    return UserResponse.from(users.save(user));
  }

  @Transactional
  public UserResponse changeRole(Long id, UpdateUserRoleRequest request) {
    User user = get(id);
    user.setRole(request.role());
    return UserResponse.from(users.save(user));
  }

  @Transactional
  public UserResponse changeStatus(Long id, UpdateUserStatusRequest request, String adminEmail) {
    User user = get(id);
    if (user.getEmail().equalsIgnoreCase(adminEmail) && request.status() == UserStatus.INACTIVE)
      throw new BadRequestException("You cannot deactivate your own account");
    user.setStatus(request.status());
    return UserResponse.from(users.save(user));
  }

  private User get(Long id) {
    return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
