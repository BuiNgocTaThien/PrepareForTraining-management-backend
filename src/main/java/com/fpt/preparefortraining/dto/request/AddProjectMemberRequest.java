package com.fpt.preparefortraining.dto.request;

import jakarta.validation.constraints.*;

public record AddProjectMemberRequest(@NotBlank @Email String email) {}
