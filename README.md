<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/MinIO-C7202C?style=for-the-badge&logo=minio&logoColor=white" alt="MinIO" />
</div>

<h1 align="center">PrepareForTraining - KBase (Backend)</h1>

<p align="center">
  <strong>Hệ thống Quản trị Tri thức và Lưu trữ Tài liệu Dự án (Knowledge Base System)</strong><br>
  <i>Cung cấp RESTful APIs mạnh mẽ cho ứng dụng KBase, bảo mật dữ liệu với chuẩn mã hóa hiện đại và quản lý tài nguyên linh hoạt.</i>
</p>

---

## 📖 Giới thiệu (Overview)

**KBase (Knowledge Base)** là giải pháp chuyển đổi số giúp các tổ chức, doanh nghiệp quản lý tài liệu dự án tập trung. Backend của KBase được xây dựng dựa trên kiến trúc **Spring Boot**, đảm nhận việc xử lý logic phân quyền (RBAC), quản lý file kích thước lớn (Media/Docs) qua AWS S3/MinIO, và cung cấp API với tốc độ phản hồi cực nhanh.

Dự án này là minh chứng cho việc áp dụng các best practices trong lập trình Java: Security, Exception Handling, File Streaming, và JPA/Hibernate optimization.

## ✨ Tính năng nổi bật (Key Features)

- 🔐 **Xác thực & Phân quyền (Security & RBAC):** 
  - Đăng nhập JWT Stateless an toàn.
  - Hỗ trợ đăng nhập một chạm với Google OAuth2.
  - Phân cấp quyền hạn 3 lớp (Admin, Owner, User).
- 🗂 **Quản lý Không gian làm việc (Workspaces/Projects):**
  - Khởi tạo, ghim, đánh dấu sao và đóng băng dự án.
  - Quản lý và mời thành viên vào dự án độc lập.
- ☁️ **Lưu trữ Đám mây (Object Storage):**
  - Stream dữ liệu trực tiếp lên MinIO/AWS S3 (Không làm nghẽn RAM server).
  - Hỗ trợ mọi định dạng tài liệu (PDF, Word, Excel, Hình ảnh, Video).
- 📧 **Hệ thống Email Tự động:**
  - Tích hợp Spring Mail và Thymeleaf (HTML Template).
  - Gửi mail Async (chạy ngầm) chào mừng user mới và cấp lại mật khẩu.

## 🛠 Tech Stack (Công nghệ sử dụng)

- **Ngôn ngữ:** Java 17+
- **Framework:** Spring Boot 3.3.x (Web, Data JPA, Security, Mail, Validation)
- **Cơ sở dữ liệu:** PostgreSQL 15+
- **Object Storage:** MinIO (AWS S3 SDK)
- **Tài liệu API:** OpenAPI 3.0 (Swagger UI)
- **Công cụ Build:** Apache Maven

## 🚀 Hướng dẫn Cài đặt (Getting Started)

### Yêu cầu hệ thống (Prerequisites)
- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) hoặc mới hơn.
- [Maven 3.8+](https://maven.apache.org/).
- [PostgreSQL](https://www.postgresql.org/) (hoặc chạy qua Docker).
- [MinIO Server](https://min.io/) (Khuyến nghị chạy qua Docker).

### Các bước khởi chạy (Run Locally)

**1. Clone mã nguồn và cấu hình Database**
Tạo một cơ sở dữ liệu trống trong PostgreSQL:
```sql
CREATE DATABASE prepare_for_training;
```

**2. Thiết lập Biến môi trường (Environment Variables)**
Copy file `src/main/resources/application-example.yml` thành `application.yml` (hoặc cấu hình trực tiếp qua biến môi trường).
Đảm bảo bạn đã điền đúng:
- Thông tin kết nối DB (`spring.datasource.url/username/password`).
- Chuỗi bí mật JWT (`app.jwt.secret` - tối thiểu 32 ký tự).
- Thông tin SMTP Gmail (`spring.mail.username/password`).
- Thông tin MinIO (`app.minio.url/accessKey/secretKey`).

**3. Khởi chạy ứng dụng**
Tại thư mục gốc, mở Terminal và chạy:
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

**4. Kiểm tra sức khỏe (Health Check)**
Truy cập trình duyệt để đảm bảo server đã chạy:
```http
GET http://localhost:8080/api/v1/health
```

## 📚 Tài liệu Hệ thống (Documentation)

Dự án được tài liệu hóa cực kỳ chi tiết theo chuẩn doanh nghiệp. Vui lòng tham khảo các thư mục sau:

- 🧭 **[Bản đồ Code (README_CODE.md)](./README_CODE.md):** Hướng dẫn nhanh cách đọc hiểu và điều hướng cấu trúc Codebase.
- 📐 **[Tài liệu Phân tích (docs/)](./docs):** Chứa toàn bộ BRD, PRD, SRS, SDD, Database Design và API Contracts.
- 🟢 **Swagger UI:** Khi server chạy thành công, mở đường dẫn `http://localhost:8080/swagger-ui/index.html` để test trực tiếp API.

## 🔒 Bảo mật (Security Notice)
- **Không bao giờ commit (push) file chứa mật khẩu thật** (DB password, Email App Password, JWT Secret) lên GitHub.
- Hãy luôn sử dụng file `.env` hoặc cấu hình Local Profile cho các khóa bí mật.

---
*Phát triển bởi đội ngũ KBase.*
