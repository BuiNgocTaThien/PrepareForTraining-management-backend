package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  List<ProjectMember> findByProjectId(Long projectId);

  void deleteByProjectIdAndUserId(Long projectId, Long userId);

  @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT pm.user) FROM ProjectMember pm WHERE (:isAdmin = true AND pm.project.status = 'ACTIVE') OR (:isAdmin = false AND pm.project.status = 'ACTIVE' AND pm.project.id IN (SELECT pm2.project.id FROM ProjectMember pm2 WHERE pm2.user.id = :userId))")
  long countByProjects(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("isAdmin") boolean isAdmin);
}
