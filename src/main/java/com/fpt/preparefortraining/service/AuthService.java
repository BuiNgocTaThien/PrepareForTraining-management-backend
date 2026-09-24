package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.UserRepository;
import com.fpt.preparefortraining.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    if (users.existsByEmail(email)) throw new BadRequestException("Email is already registered");
    User user = new User();
    user.setEmail(email);
    user.setFullName(request.fullName().trim());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    user.setStatus(UserStatus.ACTIVE);
    user = users.save(user);
    return authResponse(user);
  }

  public AuthResponse login(LoginRequest request) {
    User user =
        users
            .findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
    if (user.getStatus() != UserStatus.ACTIVE
        || !passwordEncoder.matches(request.password(), user.getPasswordHash()))
      throw new UnauthorizedException("Invalid email or password");
    return authResponse(user);
  }

  public UserResponse currentUser(String email) {
    return UserResponse.from(
        users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found")));
  }

  private AuthResponse authResponse(User user) {
    return new AuthResponse(jwtService.generate(user), "Bearer", UserResponse.from(user));
  }
}
