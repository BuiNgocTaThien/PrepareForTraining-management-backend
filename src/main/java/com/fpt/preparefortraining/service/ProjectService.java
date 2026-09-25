package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {
  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final UserRepository users;
  private final ProjectStarRepository projectStars;

  public ProjectService(
      ProjectRepository projects, ProjectMemberRepository members, UserRepository users, ProjectStarRepository projectStars) {
    this.projects = projects;
    this.members = members;
    this.users = users;
    this.projectStars = projectStars;
  }

  public Page<ProjectResponse> list(String email, Pageable pageable, String filter) {
    User actor = actor(email);
    Page<Project> result;
    
    ProjectStatus status = "archived".equalsIgnoreCase(filter) ? ProjectStatus.ARCHIVED : ProjectStatus.ACTIVE;
    
    if ("starred".equalsIgnoreCase(filter)) {
        result = projects.findStarredByUserIdAndStatus(actor.getId(), status, pageable);
    } else if ("owned".equalsIgnoreCase(filter)) {
        result = projects.findOwnedByUserIdAndStatus(actor.getId(), status, pageable);
    } else if ("shared".equalsIgnoreCase(filter)) {
        result = projects.findSharedWithUserIdAndStatus(actor.getId(), status, pageable);
    } else {
        result = actor.getRole() == Role.ADMIN
            ? projects.findAllByStatus(status, pageable)
            : projects.findVisibleByUserIdAndStatus(actor.getId(), status, pageable);
    }
            
    List<Long> projectIds = result.getContent().stream().map(Project::getId).toList();
    List<Long> starredIds = projectIds.isEmpty() ? List.of() : projectStars.findStarredProjectIds(actor.getId(), projectIds);

    return result.map(p -> ProjectResponse.from(p, starredIds.contains(p.getId())));
  }

  public DashboardStatsResponse getStats(String email) {
      User actor = actor(email);
      // For simplicity, just return total projects user is part of, and dummy counts for documents and members (or count them if repositories are available).
      // Since we don't have DocumentRepository yet, let's just count from ProjectRepository
      long activeProjects = actor.getRole() == Role.ADMIN 
            ? projects.countByStatus(ProjectStatus.ACTIVE) 
            : projects.countVisibleByUserIdAndStatus(actor.getId(), ProjectStatus.ACTIVE);
      
      // Member count: total members in those projects.
      long totalMembers = members.countByProjects(actor.getId(), actor.getRole() == Role.ADMIN);
      
      // Total docs: we don't have a document repository in this context easily accessible.
      long totalDocuments = 0; // We'll leave it 0 or mock for now, until Document features are fully fleshed out

      return new DashboardStatsResponse(activeProjects, totalDocuments, totalMembers);
  }

  @Transactional
  public ProjectResponse create(CreateProjectRequest request, String email) {
    User owner = actor(email);
    if (owner.getRole() != Role.OWNER && owner.getRole() != Role.ADMIN)
      throw new ForbiddenException("Only Owner or Admin can create a project");
    Project project = new Project();
    project.setName(request.name().trim());
    project.setDescription(blankToNull(request.description()));
    project.setOwner(owner);
    project.setStatus(ProjectStatus.ACTIVE);
    project = projects.save(project);
    ProjectMember membership = new ProjectMember();
    membership.setProject(project);
    membership.setUser(owner);
    members.save(membership);
    return ProjectResponse.from(project);
  }

  public ProjectResponse detail(Long projectId, String email) {
    Project project = get(projectId);
    requireMemberOrAdmin(project, actor(email));
    return ProjectResponse.from(project);
  }

  @Transactional
  public ProjectResponse update(Long projectId, UpdateProjectRequest request, String email) {
    Project project = get(projectId);
    requireOwnerOrAdmin(project, actor(email));
    project.setName(request.name().trim());
    project.setDescription(blankToNull(request.description()));
    return ProjectResponse.from(projects.save(project));
  }

  @Transactional
  public ProjectResponse archive(Long projectId, String email) {
    Project project = get(projectId);
    requireOwnerOrAdmin(project, actor(email));
    project.setStatus(ProjectStatus.ARCHIVED);
    return ProjectResponse.from(projects.save(project));
  }

  @Transactional
  public ProjectResponse restore(Long projectId, String email) {
    Project project = get(projectId);
    requireOwnerOrAdmin(project, actor(email));
    project.setStatus(ProjectStatus.ACTIVE);
    return ProjectResponse.from(projects.save(project));
  }

  @Transactional
  public ProjectResponse togglePin(Long projectId, String email) {
    Project project = get(projectId);
    // User requested that any member can pin
    requireMemberOrAdmin(project, actor(email));
    project.setPinned(!project.isPinned());
    return ProjectResponse.from(projects.save(project));
  }

  @Transactional
  public ProjectResponse toggleStar(Long projectId, String email) {
    Project project = get(projectId);
    User actor = actor(email);
    requireMemberOrAdmin(project, actor);
    
    ProjectStarId starId = new ProjectStarId(project.getId(), actor.getId());
    boolean currentlyStarred = projectStars.existsById(starId);
    
    if (currentlyStarred) {
      projectStars.deleteById(starId);
    } else {
      ProjectStar star = new ProjectStar();
      star.setId(starId);
      star.setProject(project);
      star.setUser(actor);
      projectStars.save(star);
    }
    
    return ProjectResponse.from(project, !currentlyStarred);
  }

  public List<ProjectMemberResponse> members(Long projectId, String email) {
    Project project = get(projectId);
    requireMemberOrAdmin(project, actor(email));
    return members.findByProjectId(projectId).stream().map(ProjectMemberResponse::from).toList();
  }

  @Transactional
  public ProjectMemberResponse addMember(
      Long projectId, AddProjectMemberRequest request, String email) {
    Project project = get(projectId);
    requireOwnerOrAdmin(project, actor(email));
    if (project.getStatus() != ProjectStatus.ACTIVE)
      throw new BadRequestException("Cannot add members to an archived project");
    User newMember =
        users
            .findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (newMember.getStatus() != UserStatus.ACTIVE)
      throw new BadRequestException("User account is inactive");
    if (members.existsByProjectIdAndUserId(projectId, newMember.getId()))
      throw new BadRequestException("User is already a project member");
    ProjectMember membership = new ProjectMember();
    membership.setProject(project);
    membership.setUser(newMember);
    return ProjectMemberResponse.from(members.save(membership));
  }

  @Transactional
  public void removeMember(Long projectId, Long userId, String email) {
    Project project = get(projectId);
    requireOwnerOrAdmin(project, actor(email));
    if (project.getOwner().getId().equals(userId))
      throw new BadRequestException("Project owner cannot be removed");
    if (!members.existsByProjectIdAndUserId(projectId, userId))
      throw new ResourceNotFoundException("Project member not found");
    members.deleteByProjectIdAndUserId(projectId, userId);
  }

  private User actor(String email) {
    User user =
        users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found"));
    if (user.getStatus() != UserStatus.ACTIVE)
      throw new UnauthorizedException("User account is inactive");
    return user;
  }

  private Project get(Long id) {
    return projects
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private void requireMemberOrAdmin(Project p, User user) {
    if (user.getRole() != Role.ADMIN
        && !members.existsByProjectIdAndUserId(p.getId(), user.getId()))
      throw new ForbiddenException("You are not a project member");
  }

  private void requireOwnerOrAdmin(Project p, User user) {
    if (user.getRole() != Role.ADMIN && !p.getOwner().getId().equals(user.getId()))
      throw new ForbiddenException("Only the project owner can perform this action");
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
