package com.fpt.preparefortraining.dto.request;

import jakarta.validation.constraints.*;

public record UpdateProjectRequest(
    @NotBlank @Size(max = 150) String name, @Size(max = 5000) String description) {}
