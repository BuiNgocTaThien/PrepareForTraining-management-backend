package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  List<ProjectMember> findByProjectId(Long projectId);

  void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
