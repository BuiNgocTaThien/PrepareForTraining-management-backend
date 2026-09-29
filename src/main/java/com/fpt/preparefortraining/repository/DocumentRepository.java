package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  List<Document> findByProjectIdOrderByCreatedAtDesc(Long projectId);

  @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM Document d WHERE (:isAdmin = true AND d.project.status = 'ACTIVE') OR (:isAdmin = false AND d.project.status = 'ACTIVE' AND d.project.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId))")
  long countDashboardDocuments(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("isAdmin") boolean isAdmin);

  long countByProjectIdAndContentTypeStartingWith(Long projectId, String prefix);

  @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM Document d WHERE d.project.id = :projectId AND d.contentType NOT LIKE 'image/%' AND d.contentType NOT LIKE 'video/%' AND d.contentType NOT LIKE 'audio/%'")
  long countOtherDocumentsByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
}
