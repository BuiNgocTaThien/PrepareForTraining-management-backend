package com.fpt.preparefortraining.repository;

import com.fpt.preparefortraining.entity.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  List<Document> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
