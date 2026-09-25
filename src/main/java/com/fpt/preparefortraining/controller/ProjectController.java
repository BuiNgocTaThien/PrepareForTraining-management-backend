package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
  private final ProjectService projects;

  public ProjectController(ProjectService projects) {
    this.projects = projects;
  }

  @GetMapping
  @Operation(summary = "List projects visible to current user")
  public ApiResponse<Page<ProjectResponse>> list(
      Authentication auth,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String filter) {
    return ApiResponse.success(projects.list(auth.getName(), PageRequest.of(page, size), filter));
  }

  @GetMapping("/stats")
  @Operation(summary = "Get dashboard stats for current user")
  public ApiResponse<DashboardStatsResponse> stats(Authentication auth) {
    return ApiResponse.success(projects.getStats(auth.getName()));
  }

  @PostMapping
  @Operation(summary = "Create a project (Owner or Admin)")
  public ResponseEntity<ApiResponse<ProjectResponse>> create(
      @Valid @RequestBody CreateProjectRequest request, Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(projects.create(request, auth.getName())));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get project details")
  public ApiResponse<ProjectResponse> detail(@PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.detail(id, auth.getName()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a project (Owner or Admin)")
  public ApiResponse<ProjectResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody UpdateProjectRequest request,
      Authentication auth) {
    return ApiResponse.success(projects.update(id, request, auth.getName()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Archive a project (Owner or Admin)")
  public ApiResponse<ProjectResponse> archive(@PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.archive(id, auth.getName()));
  }

  @PutMapping("/{id}/restore")
  @Operation(summary = "Restore an archived project (Owner or Admin)")
  public ApiResponse<ProjectResponse> restore(@PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.restore(id, auth.getName()));
  }

  @PutMapping("/{id}/pin")
  @Operation(summary = "Toggle pin status of a project (Owner or Admin)")
  public ApiResponse<ProjectResponse> togglePin(@PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.togglePin(id, auth.getName()));
  }

  @PutMapping("/{id}/star")
  @Operation(summary = "Toggle star status of a project for current user")
  public ApiResponse<ProjectResponse> toggleStar(@PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.toggleStar(id, auth.getName()));
  }

  @GetMapping("/{id}/members")
  @Operation(summary = "List project members")
  public ApiResponse<List<ProjectMemberResponse>> members(
      @PathVariable Long id, Authentication auth) {
    return ApiResponse.success(projects.members(id, auth.getName()));
  }

  @PostMapping("/{id}/members")
  @Operation(summary = "Add a member by email (Owner or Admin)")
  public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
      @PathVariable Long id,
      @Valid @RequestBody AddProjectMemberRequest request,
      Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(projects.addMember(id, request, auth.getName())));
  }

  @DeleteMapping("/{id}/members/{userId}")
  @Operation(summary = "Remove a project member (Owner or Admin)")
  public ApiResponse<Void> removeMember(
      @PathVariable Long id, @PathVariable Long userId, Authentication auth) {
    projects.removeMember(id, userId, auth.getName());
    return ApiResponse.success(null);
  }
}
