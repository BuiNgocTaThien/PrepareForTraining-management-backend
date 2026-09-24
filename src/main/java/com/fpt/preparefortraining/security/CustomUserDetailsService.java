package com.fpt.preparefortraining.security;

import com.fpt.preparefortraining.entity.UserStatus;
import com.fpt.preparefortraining.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public CustomUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    var user =
        users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return User.withUsername(user.getEmail())
        .password(user.getPasswordHash())
        .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        .disabled(user.getStatus() != UserStatus.ACTIVE)
        .build();
  }
}
