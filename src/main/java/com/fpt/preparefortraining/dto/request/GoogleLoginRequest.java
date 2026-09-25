package com.fpt.preparefortraining.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "Access token is required") String accessToken
) {}
