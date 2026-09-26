package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.response.DocumentResponse;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.*;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class DocumentService {
  private final DocumentRepository documents;
  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final UserRepository users;
  private final FileStorageService storageService;

  public DocumentService(
      DocumentRepository documents,
      ProjectRepository projects,
      ProjectMemberRepository members,
      UserRepository users,
      FileStorageService storageService) {
    this.documents = documents;
    this.projects = projects;
    this.members = members;
    this.users = users;
    this.storageService = storageService;
  }

  public List<DocumentResponse> listByProject(Long projectId, String email) {
    Project project = getProject(projectId);
    requireMemberOrAdmin(project, actor(email));
    return documents.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
        .map(DocumentResponse::from)
        .toList();
  }

  @Transactional
  public DocumentResponse upload(Long projectId, MultipartFile file, String email) {
    Project project = getProject(projectId);
    User actor = actor(email);
    requireMemberOrAdmin(project, actor);

    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null || originalFileName.isEmpty()) {
      throw new BadRequestException("File name is invalid");
    }

    String extension = "";
    int i = originalFileName.lastIndexOf('.');
    if (i > 0) {
      extension = originalFileName.substring(i);
    }
    String objectName = "project_" + projectId + "/" + UUID.randomUUID() + extension;

    try {
      storageService.uploadFile(
          objectName, file.getInputStream(), file.getContentType(), file.getSize());
    } catch (Exception e) {
      throw new InternalServerException("Failed to upload file: " + e.getMessage());
    }

    Document doc = new Document();
    doc.setProject(project);
    doc.setUploader(actor);
    doc.setFileName(originalFileName);
    doc.setFileSize(file.getSize());
    doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
    doc.setStoragePath(objectName);
    doc = documents.save(doc);

    return DocumentResponse.from(doc);
  }

  public InputStream download(Long documentId, String email) {
    Document doc = get(documentId);
    requireMemberOrAdmin(doc.getProject(), actor(email));
    return storageService.downloadFile(doc.getStoragePath());
  }
  
  public Document getDocumentDetail(Long documentId, String email) {
      Document doc = get(documentId);
      requireMemberOrAdmin(doc.getProject(), actor(email));
      return doc;
  }

  @Transactional
  public void delete(Long documentId, String email) {
    Document doc = get(documentId);
    User actor = actor(email);
    
    // Only the uploader, project owner, or admin can delete a document
    if (actor.getRole() != Role.ADMIN 
        && !doc.getUploader().getId().equals(actor.getId()) 
        && !doc.getProject().getOwner().getId().equals(actor.getId())) {
        throw new ForbiddenException("You do not have permission to delete this document");
    }

    storageService.deleteFile(doc.getStoragePath());
    documents.delete(doc);
  }

  private User actor(String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found"));
    if (user.getStatus() != UserStatus.ACTIVE) throw new UnauthorizedException("User inactive");
    return user;
  }

  private Project getProject(Long id) {
    return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private Document get(Long id) {
    return documents.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document not found"));
  }

  private void requireMemberOrAdmin(Project p, User user) {
    if (user.getRole() != Role.ADMIN && !members.existsByProjectIdAndUserId(p.getId(), user.getId())) {
      throw new ForbiddenException("You are not a project member");
    }
  }

  @Transactional
  public DocumentResponse rename(Long documentId, String newName, String email) {
    Document doc = get(documentId);
    User actor = actor(email);
    
    if (actor.getRole() != Role.ADMIN 
        && !doc.getUploader().getId().equals(actor.getId()) 
        && !doc.getProject().getOwner().getId().equals(actor.getId())) {
        throw new ForbiddenException("You do not have permission to rename this document");
    }

    if (newName == null || newName.trim().isEmpty()) {
        throw new BadRequestException("New name cannot be empty");
    }

    doc.setFileName(newName.trim());
    doc = documents.save(doc);
    return DocumentResponse.from(doc);
  }
}
