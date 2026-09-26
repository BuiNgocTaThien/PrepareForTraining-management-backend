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

  // --- Lấy danh sách toàn bộ tài liệu trong một Dự án ---
  public List<DocumentResponse> listByProject(Long projectId, String email) {
    Project project = getProject(projectId); // Lấy project từ DB
    requireMemberOrAdmin(project, actor(email)); // Kiểm tra quyền: Người gọi phải là thành viên dự án hoặc Admin
    
    // Tìm các tài liệu thuộc project, sắp xếp theo thời gian tạo mới nhất và trả về
    return documents.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
        .map(DocumentResponse::from)
        .toList();
  }

  // --- Tính năng Upload Tài liệu lên hệ thống ---
  @Transactional
  public DocumentResponse upload(Long projectId, MultipartFile file, String email) {
    Project project = getProject(projectId);
    User actor = actor(email);
    requireMemberOrAdmin(project, actor); // Xác minh quyền trước khi cho upload

    // Lấy tên gốc của file do người dùng tải lên
    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null || originalFileName.isEmpty()) {
      throw new BadRequestException("File name is invalid");
    }

    // Tách phần đuôi mở rộng (extension) của file (VD: .pdf, .docx)
    String extension = "";
    int i = originalFileName.lastIndexOf('.');
    if (i > 0) {
      extension = originalFileName.substring(i);
    }
    
    // Sinh ra tên file độc nhất để lưu vào Object Storage (MinIO/S3) tránh trùng lặp
    // Định dạng: project_{id}/uuid.extension
    String objectName = "project_" + projectId + "/" + UUID.randomUUID() + extension;

    try {
      // Đẩy luồng dữ liệu (stream) lên MinIO server
      storageService.uploadFile(
          objectName, file.getInputStream(), file.getContentType(), file.getSize());
    } catch (Exception e) {
      throw new InternalServerException("Failed to upload file: " + e.getMessage());
    }

    // Chỉ lưu siêu dữ liệu (metadata) của file vào cơ sở dữ liệu PostgreSQL
    Document doc = new Document();
    doc.setProject(project);
    doc.setUploader(actor); // Đánh dấu ai là người upload
    doc.setFileName(originalFileName); // Lưu lại tên gốc để người dùng dễ nhìn
    doc.setFileSize(file.getSize());
    doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
    doc.setStoragePath(objectName); // Lưu đường dẫn ảo trên S3 để sau này download
    doc = documents.save(doc);

    return DocumentResponse.from(doc);
  }

  // --- Tính năng Download file ---
  public InputStream download(Long documentId, String email) {
    Document doc = get(documentId);
    requireMemberOrAdmin(doc.getProject(), actor(email)); // Phải là thành viên dự án mới được tải
    // Yêu cầu MinIO trả về luồng stream file
    return storageService.downloadFile(doc.getStoragePath());
  }
  
  public Document getDocumentDetail(Long documentId, String email) {
      Document doc = get(documentId);
      requireMemberOrAdmin(doc.getProject(), actor(email));
      return doc;
  }

  // --- Tính năng Xóa tài liệu ---
  @Transactional
  public void delete(Long documentId, String email) {
    Document doc = get(documentId);
    User actor = actor(email);
    
    // Quy tắc: Chỉ người tạo ra file (uploader), chủ dự án (owner) hoặc Admin hệ thống mới có quyền xóa
    if (actor.getRole() != Role.ADMIN 
        && !doc.getUploader().getId().equals(actor.getId()) 
        && !doc.getProject().getOwner().getId().equals(actor.getId())) {
        throw new ForbiddenException("You do not have permission to delete this document");
    }

    // Xóa file vật lý trên MinIO trước
    storageService.deleteFile(doc.getStoragePath());
    // Xóa bản ghi trong Database
    documents.delete(doc);
  }

  // --- Các hàm Validate dùng chung ---
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

  // --- Kiểm tra xem user có quyền truy cập vào project hay không ---
  private void requireMemberOrAdmin(Project p, User user) {
    if (user.getRole() != Role.ADMIN && !members.existsByProjectIdAndUserId(p.getId(), user.getId())) {
      throw new ForbiddenException("You are not a project member");
    }
  }

  // --- Tính năng đổi tên tài liệu (Rename) ---
  @Transactional
  public DocumentResponse rename(Long documentId, String newName, String email) {
    Document doc = get(documentId);
    User actor = actor(email);
    
    // Quy tắc ủy quyền tương tự như xóa
    if (actor.getRole() != Role.ADMIN 
        && !doc.getUploader().getId().equals(actor.getId()) 
        && !doc.getProject().getOwner().getId().equals(actor.getId())) {
        throw new ForbiddenException("You do not have permission to rename this document");
    }

    if (newName == null || newName.trim().isEmpty()) {
        throw new BadRequestException("New name cannot be empty");
    }

    doc.setFileName(newName.trim()); // Chỉ đổi tên metadata (không đổi Object Path trên MinIO)
    doc = documents.save(doc);
    return DocumentResponse.from(doc);
  }
}
