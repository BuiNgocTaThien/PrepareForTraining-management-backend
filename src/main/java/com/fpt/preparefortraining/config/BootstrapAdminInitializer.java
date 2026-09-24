package com.fpt.preparefortraining.config;

import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminInitializer {
  @Bean
  CommandLineRunner bootstrapAdmin(
      UserRepository users,
      PasswordEncoder encoder,
      @Value("${app.bootstrap-admin.email:}") String email,
      @Value("${app.bootstrap-admin.password:}") String password,
      @Value("${app.bootstrap-admin.full-name:Administrator}") String fullName) {
    return args -> {
      if (email == null || email.isBlank() || password == null || password.isBlank()) return;
      
      String targetEmail = email.trim().toLowerCase();
      
      users.findByEmail(targetEmail).ifPresentOrElse(
        existingUser -> {
          // Force update to ADMIN if they exist but aren't ADMIN
          if (existingUser.getRole() != Role.ADMIN) {
            existingUser.setRole(Role.ADMIN);
            users.save(existingUser);
          }
        },
        () -> {
          User admin = new User();
          admin.setEmail(targetEmail);
          admin.setFullName(fullName);
          admin.setPasswordHash(encoder.encode(password));
          admin.setRole(Role.ADMIN);
          admin.setStatus(UserStatus.ACTIVE);
          users.save(admin);
        }
      );
    };
  }
}
