package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.ProjectStar;
import com.fpt.preparefortraining.entity.ProjectStarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectStarRepository extends JpaRepository<ProjectStar, ProjectStarId> {
  @Query("SELECT ps.project.id FROM ProjectStar ps WHERE ps.user.id = :userId AND ps.project.id IN :projectIds")
  List<Long> findStarredProjectIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);
}
