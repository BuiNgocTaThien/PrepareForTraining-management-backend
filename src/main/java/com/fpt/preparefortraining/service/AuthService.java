package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.UserRepository;
import com.fpt.preparefortraining.repository.PasswordResetTokenRepository;
import com.fpt.preparefortraining.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;

  public AuthService(UserRepository users, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
    this.users = users;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.emailService = emailService;
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

    // Send Welcome Email
    emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

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

  @Transactional
  public AuthResponse googleLogin(GoogleLoginRequest request) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(request.accessToken());
    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
    
    ResponseEntity<Map> response;
    try {
        response = restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, entity, Map.class);
    } catch(Exception e) {
        throw new UnauthorizedException("Invalid Google access token");
    }

    Map<String, Object> payload = response.getBody();
    if(payload == null || !payload.containsKey("email")) {
        throw new UnauthorizedException("Invalid Google access token");
    }

    String email = ((String) payload.get("email")).trim().toLowerCase();
    String name = (String) payload.get("name");
    
    boolean isNewUser = !users.existsByEmail(email);

    User user = users.findByEmail(email).orElseGet(() -> {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : "Google User");
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole(Role.USER);
        newUser.setStatus(UserStatus.ACTIVE);
        return users.save(newUser);
    });

    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new UnauthorizedException("User account is deactivated");
    }

    if (isNewUser) {
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    return authResponse(user);
  }

  public UserResponse currentUser(String email) {
    return UserResponse.from(
        users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found")));
  }

  @Transactional
  public void forgotPassword(String email) {
      User user = users.findByEmail(email)
              .orElseThrow(() -> new UnauthorizedException("User not found with email " + email));

      if (user.getStatus() != UserStatus.ACTIVE) {
          throw new UnauthorizedException("User account is deactivated");
      }

      passwordResetTokenRepository.deleteByUser(user);

      String token = UUID.randomUUID().toString();
      PasswordResetToken resetToken = new PasswordResetToken();
      resetToken.setToken(token);
      resetToken.setUser(user);
      resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
      passwordResetTokenRepository.save(resetToken);

      String resetUrl = "http://localhost:5173/reset-password?token=" + token;
      emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
      PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
              .orElseThrow(() -> new UnauthorizedException("Invalid or expired password reset token"));

      if (resetToken.isExpired()) {
          passwordResetTokenRepository.delete(resetToken);
          throw new UnauthorizedException("Password reset token has expired");
      }

      User user = resetToken.getUser();
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      users.save(user);

      passwordResetTokenRepository.delete(resetToken);
  }

  private AuthResponse authResponse(User user) {
    return new AuthResponse(jwtService.generate(user), "Bearer", UserResponse.from(user));
  }
}
