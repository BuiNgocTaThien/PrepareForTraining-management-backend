# Hợp đồng API (API Contract — v1)

**Base URL**: `http://localhost:8080/api/v1`
*Lưu ý*: Các endpoint được bảo vệ yêu cầu Header `Authorization: Bearer <token>`.

## 1. Danh sách các Endpoints

| Phương thức (Method) | Endpoint | Quyền truy cập (Role) | Mục đích (Purpose) |
| --- | --- | --- | --- |
| **POST** | `/auth/register` | Public (Công khai) | Đăng ký người dùng mới. |
| **POST** | `/auth/login` | Public (Công khai) | Đăng nhập và nhận chuỗi JWT. |
| **GET** | `/users/me` | Đã xác thực (Authenticated) | Lấy thông tin hồ sơ của phiên đăng nhập hiện tại. |
| **GET** | `/projects` | Đã xác thực (Authenticated) | Lấy danh sách các dự án mà người dùng được phép thấy (hỗ trợ phân trang bằng `page` và `size`). |
| **POST** | `/projects` | OWNER, ADMIN | Tạo một dự án mới. |
| **GET** | `/projects/{id}` | Member, ADMIN | Xem chi tiết thông tin của dự án. |
| **PUT** | `/projects/{id}` | Owner, ADMIN | Cập nhật thông tin dự án. |
| **DELETE** | `/projects/{id}` | Owner, ADMIN | Lưu trữ (archive) dự án thay vì xoá vĩnh viễn. |
| **GET** | `/projects/{id}/members` | Member, ADMIN | Lấy danh sách thành viên trong dự án. |
| **POST** | `/projects/{id}/members` | Owner, ADMIN | Thêm thành viên vào dự án thông qua User ID hoặc Email. |
| **DELETE**| `/projects/{id}/members/{userId}` | Owner, ADMIN | Xoá một thành viên khỏi dự án. |
| **GET** | `/projects/{id}/documents` | Member, ADMIN | Liệt kê metadata của các tài liệu trong dự án. |
| **POST** | `/projects/{id}/documents` | Member, ADMIN | Upload tài liệu (Multipart form-data) với field là `file`. |
| **GET** | `/documents/{id}/download` | Member, ADMIN | Tải xuống file gốc đã lưu trữ. |
| **DELETE**| `/documents/{id}` | Uploader, Owner, ADMIN | Xoá file tài liệu khỏi cơ sở dữ liệu và hệ thống lưu trữ MinIO. |

## 2. Cấu trúc Response chuẩn (Envelope)

Mọi API trả về dữ liệu đều được bao bọc (wrap) bởi một đối tượng JSON chuẩn:

```json
{
  "success": true,
  "message": "Thành công",
  "data": { ... } // Dữ liệu trả về hoặc Array/Page data
}
```

## 3. Ví dụ Payload Xác thực (Auth Examples)

**Đăng ký tài khoản mới (`POST /auth/register`)**

```json
{
  "email": "owner@example.com",
  "password": "Password123!",
  "fullName": "Project Owner",
  "role": "OWNER"
}
```

**Đăng nhập hệ thống (`POST /auth/login`)**

```json
{
  "email": "owner@example.com",
  "password": "Password123!"
}
```

> **Ghi chú quan trọng cho lập trình viên:** 
> Hãy chắc chắn implement chính xác các DTOs (Data Transfer Objects) và quy tắc Validate dữ liệu đầu vào phía Backend trước khi bắt tay vào ghép giao diện Frontend. Trang bị phân trang (`Pageable`) cho các danh sách trả về thay vì List toàn bộ dữ liệu.
