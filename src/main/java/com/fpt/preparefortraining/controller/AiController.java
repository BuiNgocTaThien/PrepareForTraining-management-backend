package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.request.ChatRequest;
import com.fpt.preparefortraining.dto.response.ApiResponse;
import com.fpt.preparefortraining.dto.response.ChatResponse;
import com.fpt.preparefortraining.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.fpt.preparefortraining.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;
    private final ProjectService projectService;

    @Autowired
    public AiController(AiService aiService, ProjectService projectService) {
        this.aiService = aiService;
        this.projectService = projectService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Ask AI chatbot about a project")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        // Validate user has access to the project
        projectService.detail(request.getProjectId(), auth.getName());
        return ApiResponse.success(aiService.askChatbot(request));
    }
    
    @PostMapping("/reload/{projectId}")
    @Operation(summary = "Reload AI knowledge base for a project")
    public ApiResponse<Void> reloadKnowledge(@PathVariable Long projectId, Authentication auth) {
        // Validate user has access to the project
        projectService.detail(projectId, auth.getName());
        aiService.reloadKnowledge(projectId);
        return ApiResponse.success(null);
    }
}
