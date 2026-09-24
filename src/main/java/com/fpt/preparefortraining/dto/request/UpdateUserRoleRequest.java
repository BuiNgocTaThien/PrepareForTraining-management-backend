package com.fpt.preparefortraining.dto.request;

import com.fpt.preparefortraining.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {}
