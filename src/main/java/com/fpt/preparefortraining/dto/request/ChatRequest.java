package com.fpt.preparefortraining.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Question is required")
    private String question;

    private List<ChatMessage> history;
}
