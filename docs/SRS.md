# Software Requirements Specification (SRS)
## PrepareForTraining (KBase - Knowledge Base)

---

### 1. Introduction (Giới thiệu)

#### 1.1. Mục đích
Tài liệu Đặc tả Yêu cầu Phần mềm (SRS) này quy định các hành vi hệ thống, giao diện tương tác và các tiêu chuẩn phi chức năng của hệ thống KBase (PrepareForTraining). Đây là bản thiết kế tham chiếu (Single source of truth) cho Developer, Tester và Quản lý dự án.

#### 1.2. Phân loại mức độ ưu tiên
- **M (Must have)**: Bắt buộc phải có trong phiên bản MVP.
- **S (Should have)**: Quan trọng nhưng có thể trễ hạn sang bản cập nhật nhỏ.
- **C (Could have)**: Tùy chọn (Optional).
- **W (Won't have)**: Sẽ không làm ở Phase này.

---

### 2. Functional Requirements (Yêu cầu chức năng)

#### 2.1. Phân hệ Xác thực & Người dùng (Authentication)
| ID | Yêu cầu (Requirement) | Priority |
| --- | --- | --- |
| `REQ-AUTH-01` | Người dùng có thể đăng ký tài khoản bằng Email và Mật khẩu. | M |
| `REQ-AUTH-02` | Mật khẩu phải có độ dài tối thiểu 6 ký tự. | M |
| `REQ-AUTH-03` | Người dùng có thể đăng nhập bằng API lấy JWT Token. | M |
| `REQ-AUTH-04` | Hỗ trợ đăng nhập một chạm bằng Google (Sử dụng Google OAuth2 Token). | M |
| `REQ-AUTH-05` | Quên mật khẩu: Gửi email chứa token đặt lại mật khẩu. | S |
| `REQ-AUTH-06` | Đổi mật khẩu: Yêu cầu mật khẩu cũ, tự động đăng xuất sau khi đổi thành công. | M |
| `REQ-AUTH-07` | Đổi thông tin cá nhân: Có thể đổi Họ và Tên. Không được đổi Email. | M |

#### 2.2. Phân hệ Quản trị (Admin)
| ID | Yêu cầu (Requirement) | Priority |
| --- | --- | --- |
| `REQ-ADM-01` | Màn hình danh sách người dùng hiển thị phân trang (pagination). | M |
| `REQ-ADM-02` | Admin có thể tìm kiếm người dùng theo Name hoặc Email. | M |
| `REQ-ADM-03` | Admin có thể tạo trực tiếp một User mới và gán quyền (Role). | M |
| `REQ-ADM-04` | Admin có thể thay đổi Role (ADMIN, OWNER, USER) của một người dùng. | M |
| `REQ-ADM-05` | Admin có thể kích hoạt (ACTIVE) hoặc khoá (INACTIVE) người dùng. | M |

#### 2.3. Phân hệ Dự án (Projects)
| ID | Yêu cầu (Requirement) | Priority |
| --- | --- | --- |
| `REQ-PROJ-01` | Quyền OWNER có thể tạo dự án mới (Tên, Mô tả). | M |
| `REQ-PROJ-02` | Hệ thống phân loại dự án ở màn hình Dashboard: Tất cả, Dự án của tôi, Kho lưu trữ. | M |
| `REQ-PROJ-03` | Có tính năng Đánh dấu sao (Pin) để dự án luôn lên đầu danh sách. | M |
| `REQ-PROJ-04` | OWNER có thể thay đổi trạng thái dự án thành ARCHIVED (Lưu trữ). | M |
| `REQ-PROJ-05` | Tìm kiếm dự án theo tên bằng thanh công cụ tìm kiếm. | M |
| `REQ-PROJ-06` | OWNER có thể mời một User vào dự án thông qua Email. | M |
| `REQ-PROJ-07` | Hủy tư cách thành viên dự án của một người dùng. | M |

#### 2.4. Phân hệ Tài liệu (Documents)
| ID | Yêu cầu (Requirement) | Priority |
| --- | --- | --- |
| `REQ-DOC-01` | Bất kỳ thành viên nào trong dự án cũng có thể upload file. | M |
| `REQ-DOC-02` | Hỗ trợ hiển thị % tiến độ upload ở giao diện frontend. | S |
| `REQ-DOC-03` | Hệ thống chỉ chấp nhận định dạng: PDF, DOC(X), XLS(X), PPT(X), MD, TXT, Hình ảnh (JPG, PNG...), Video (MP4...). | M |
| `REQ-DOC-04` | Có khả năng đổi tên tài liệu trên hệ thống. | M |
| `REQ-DOC-05` | Tải xuống (Download) tài liệu nguyên bản. | M |
| `REQ-DOC-06` | Xóa tài liệu (Chỉ người upload hoặc OWNER dự án mới có quyền). | M |

---

### 3. Non-Functional Requirements (Yêu cầu phi chức năng)

#### 3.1. Performance (Hiệu suất)
- `NFR-PERF-01`: API Load: Thời gian xử lý các truy vấn GET (không bao gồm File) phải < 200ms với băng thông mạng ổn định.
- `NFR-PERF-02`: File Upload: Hệ thống phải xử lý trơn tru việc upload file lên tới 100MB qua S3 SDK (khuyến nghị dùng Presigned URL nếu file lớn hơn).

#### 3.2. Security (Bảo mật)
- `NFR-SEC-01`: Mật khẩu lưu trong CSDL bị băm bằng chuẩn BCrypt(strength=10).
- `NFR-SEC-02`: Không ai, kể cả Admin, có thể đọc được mật khẩu gốc của người dùng.
- `NFR-SEC-03`: Tấn công Cross-Site Scripting (XSS) được ngăn chặn bằng cơ chế sanitize của React.
- `NFR-SEC-04`: Lỗ hổng Insecure Direct Object References (IDOR) được phòng ngừa bằng cách xác thực quyền truy cập project (User có thuộc ProjectID đó không) ở Backend.

#### 3.3. Reliability & Maintenance (Độ tin cậy & Bảo trì)
- `NFR-REL-01`: Code Backend phải tuân thủ chuẩn RESTful API, sử dụng HTTP Status Code chuẩn xác (200, 201 cho thành công; 400, 401, 403, 404 cho lỗi client; 500 cho lỗi server).
- `NFR-REL-02`: Tài liệu OpenAPI 3.0 (Swagger) phải luôn được cập nhật tự động (auto-generated) và truy cập được qua link `/swagger-ui.html`.
