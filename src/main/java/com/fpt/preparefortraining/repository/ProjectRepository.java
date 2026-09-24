package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  @Query(
      "select pm.project from ProjectMember pm where pm.user.id = :userId and pm.project.status = :status")
  Page<Project> findVisibleByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, Pageable pageable);

  @Query("select ps.project from ProjectStar ps where ps.user.id = :userId and ps.project.status = :status")
  Page<Project> findStarredByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, Pageable pageable);
  
  Page<Project> findAllByStatus(com.fpt.preparefortraining.entity.ProjectStatus status, Pageable pageable);
}
