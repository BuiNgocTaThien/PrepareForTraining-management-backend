package com.fpt.preparefortraining.dto.response;

public record AuthResponse(String accessToken, String tokenType, UserResponse user) {}
