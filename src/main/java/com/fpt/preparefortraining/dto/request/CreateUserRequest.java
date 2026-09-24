package com.fpt.preparefortraining.dto.request;

import com.fpt.preparefortraining.entity.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Size(max = 150) String fullName,
    @NotNull Role role) {}
