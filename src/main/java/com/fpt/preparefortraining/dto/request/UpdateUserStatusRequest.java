package com.fpt.preparefortraining.dto.request;

import com.fpt.preparefortraining.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {}
