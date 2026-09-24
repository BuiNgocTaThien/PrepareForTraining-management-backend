# Đặc tả Yêu cầu Phần mềm (Software Requirements Specification - SRS)

## 1. Tổng quan Sản phẩm

**PrepareForTraining Management** là một hệ thống quản lý cơ sở tri thức dự án cá nhân và đội nhóm. Người dùng sau khi xác thực có thể tạo dự án, quản lý thành viên và lưu trữ các tài liệu liên quan đến dự án (file văn bản, hình ảnh, video). 
Phiên bản đầu tiên (MVP) tập trung vào việc quản lý siêu dữ liệu (metadata) của tài liệu và khả năng upload/download file. Tính năng Chatbot AI hỗ trợ tìm kiếm tài liệu là một tuỳ chọn sẽ được phát triển trong các giai đoạn sau.

## 2. Phân quyền (Roles)

| Vai trò (Role) | Quyền hạn |
| --- | --- |
| **ADMIN** | Quản trị viên hệ thống: Quản lý toàn bộ người dùng và tất cả các dự án trên hệ thống. |
| **OWNER** | Chủ dự án: Tạo dự án mới, quản lý các dự án do mình sở hữu, mời hoặc xoá thành viên khỏi dự án. |
| **USER** | Người dùng tiêu chuẩn: Xem các dự án mà họ là thành viên; tải lên (upload), xem và tải xuống (download) tài liệu trong các dự án đó. |

## 3. Yêu cầu Chức năng (Functional Requirements - MVP)

1. **Xác thực**: Khách truy cập có thể đăng ký tài khoản mới và đăng nhập bằng Email và Mật khẩu.
2. **Bảo mật**: Hệ thống API sẽ trả về một chuỗi JWT Access Token sau khi đăng nhập thành công.
3. **Quản lý Dự án**: OWNER có thể tạo mới, cập nhật thông tin, lưu trữ (archive/soft-delete) và xem danh sách các dự án của mình.
4. **Quản lý Thành viên**: OWNER có quyền thêm một người dùng đã tồn tại vào dự án (thông qua email) và xoá thành viên khỏi dự án.
5. **Quản lý Tài liệu**: Thành viên dự án có thể xem danh sách tài liệu, tải lên các định dạng file được cho phép, xem metadata và tải nguyên bản file về máy.
6. **Hỗ trợ File**: Hệ thống hỗ trợ đa dạng định dạng file: PDF, DOC/DOCX, XLS/XLSX, PPT/PPTX, MD, TXT, JPG, PNG, GIF, SVG, BMP, MP4, MOV, và AVI.
7. **Phân quyền Dữ liệu**: Người dùng chỉ có thể truy cập vào các dự án mà họ là thành viên (ngoại trừ ADMIN có đặc quyền xem tất cả).
8. **Tài liệu API**: Tự động sinh tài liệu Swagger/OpenAPI cho mọi endpoint đã hoàn thiện.

## 4. Yêu cầu Phi chức năng (Non-functional Requirements)

- **Chuẩn giao tiếp**: Tất cả các endpoint API phải bắt đầu bằng `/api/v1` và trả về định dạng JSON (ngoại trừ endpoint upload/download file).
- **Mã hoá Mật khẩu**: Toàn bộ mật khẩu phải được mã hoá bằng thuật toán BCrypt. Không bao giờ lộ mật khẩu trong response.
- **Validation**: Bắt lỗi dữ liệu đầu vào chặt chẽ và trả về thông báo lỗi chuẩn hoá. (Đã hỗ trợ phân trang).
- **Lưu trữ**: Siêu dữ liệu (metadata) lưu trong PostgreSQL; File vật lý lưu tại MinIO (hoặc Local Storage khi code local).

## 5. Các tính năng mở rộng (Out of scope for MVP)

Các tính năng sau chưa được yêu cầu trong phiên bản MVP hiện tại: Gửi email mời tự động, trích xuất chữ từ ảnh (OCR), tìm kiếm toàn văn bản (Semantic search), Chatbot AI hỏi đáp tài liệu, và triển khai hạ tầng với Kubernetes/Terraform.
