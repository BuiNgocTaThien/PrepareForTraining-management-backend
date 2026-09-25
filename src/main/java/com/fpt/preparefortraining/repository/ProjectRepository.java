package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  @Query(
      "select p from Project p join ProjectMember pm on pm.project = p where pm.user.id = :userId and p.status = :status " +
      "and lower(p.name) like lower(concat('%', :search, '%'))")
  Page<Project> findVisibleByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, @Param("search") String search, Pageable pageable);

  @Query("select p from Project p where p.owner.id = :userId and p.status = :status " +
         "and lower(p.name) like lower(concat('%', :search, '%'))")
  Page<Project> findOwnedByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, @Param("search") String search, Pageable pageable);

  @Query("select p from Project p join ProjectMember pm on pm.project = p where pm.user.id = :userId and p.owner.id != :userId and p.status = :status " +
         "and lower(p.name) like lower(concat('%', :search, '%'))")
  Page<Project> findSharedWithUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, @Param("search") String search, Pageable pageable);

  @Query("select p from Project p join ProjectStar ps on ps.project = p where ps.user.id = :userId and p.status = :status " +
         "and lower(p.name) like lower(concat('%', :search, '%'))")
  Page<Project> findStarredByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, @Param("search") String search, Pageable pageable);
  
  @Query("select p from Project p where p.status = :status " +
         "and lower(p.name) like lower(concat('%', :search, '%'))")
  Page<Project> findAllByStatus(@Param("status") com.fpt.preparefortraining.entity.ProjectStatus status, @Param("search") String search, Pageable pageable);

  long countByStatus(com.fpt.preparefortraining.entity.ProjectStatus status);

  @Query("select count(pm.project) from ProjectMember pm where pm.user.id = :userId and pm.project.status = :status")
  long countVisibleByUserIdAndStatus(@Param("userId") Long userId, @Param("status") com.fpt.preparefortraining.entity.ProjectStatus status);
}
