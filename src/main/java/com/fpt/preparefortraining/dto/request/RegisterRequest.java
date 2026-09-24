package com.fpt.preparefortraining.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Size(max = 150) String fullName) {}
