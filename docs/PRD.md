# Product Requirements Document (PRD)
## PrepareForTraining (KBase - Knowledge Base)

---

### 1. Product Vision & Goals

**Tầm nhìn (Vision):**  
Trở thành nền tảng quản lý tri thức nội bộ tối ưu nhất, giúp các đội nhóm (teams) tổ chức, lưu trữ và khai thác thông tin từ đa dạng các loại tài liệu (văn bản, hình ảnh, video) một cách an toàn, thông minh và nhanh chóng thông qua sự hỗ trợ của trí tuệ nhân tạo (AI).

**Mục tiêu (Goals):**
- Xây dựng một không gian làm việc số (Workspace) tập trung, giảm thiểu việc thất thoát dữ liệu do lưu trữ phân tán.
- Tăng hiệu suất làm việc bằng cách giúp người dùng tìm kiếm thông tin tài liệu chỉ trong vài giây.
- Thiết lập cơ chế bảo mật nghiêm ngặt: dữ liệu của dự án nào chỉ được phép truy cập bởi các thành viên thuộc dự án đó.

---

### 2. User Personas (Chân dung người dùng)

#### 2.1. Quản trị viên hệ thống (System Admin)
- **Đặc điểm**: Nắm giữ quyền hạn cao nhất của nền tảng, có kiến thức về kỹ thuật.
- **Mục tiêu**: Giám sát hoạt động của toàn bộ hệ thống, quản lý tài khoản người dùng, đảm bảo nền tảng hoạt động ổn định.
- **Nỗi đau (Pain points)**: Khó kiểm soát khi số lượng người dùng và dự án tăng lên, cần một công cụ dashboard tổng quan.

#### 2.2. Trưởng nhóm/Chủ dự án (Project Owner)
- **Đặc điểm**: Quản lý một nhóm nhỏ hoặc một dự án cụ thể. Là người trực tiếp tạo ra không gian làm việc.
- **Mục tiêu**: Tổ chức tài liệu cho team một cách khoa học. Mời đúng người vào đúng dự án. Đảm bảo nhân sự mới có thể tiếp cận tài liệu dự án dễ dàng.
- **Nỗi đau (Pain points)**: Mất nhiều thời gian để gửi lại tài liệu cho nhân sự mới. Khó quản lý quyền truy cập khi có nhân sự nghỉ việc.

#### 2.3. Thành viên dự án (Normal User)
- **Đặc điểm**: Người dùng cuối, thường xuyên đọc, tải lên và tra cứu tài liệu.
- **Mục tiêu**: Tìm kiếm tài liệu nhanh chóng để phục vụ công việc. Trao đổi và hỏi đáp về các nội dung chuyên môn có trong tài liệu.
- **Nỗi đau (Pain points)**: Chìm ngập trong hàng tá file word/pdf và không nhớ thông tin mình cần nằm ở file nào.

---

### 3. User Stories & Acceptance Criteria

#### 3.1. Quản lý tài khoản (Authentication)
**Story 1:** Là một người dùng, tôi muốn đăng ký và đăng nhập vào hệ thống an toàn để bảo mật thông tin cá nhân.
- *Acceptance Criteria (AC):*
  - Bắt buộc xác thực email hợp lệ.
  - Hỗ trợ đăng nhập một chạm bằng Google (OAuth2).
  - Password phải được băm (Bcrypt) và không bao giờ lộ ra ngoài.
  - Bắt buộc đổi mật khẩu thành công mới được tiếp tục sử dụng nếu hệ thống yêu cầu. Sau khi đổi, tự động đăng xuất và yêu cầu đăng nhập lại.

**Story 2:** Là một người dùng, tôi muốn cập nhật thông tin cá nhân (Tên hiển thị) và avatar.
- *AC:*
  - Cập nhật thành công sẽ phản ánh ngay trên giao diện (AppShell) mà không cần tải lại toàn bộ trang.

#### 3.2. Bảng điều khiển Quản trị (Admin Dashboard)
**Story 3:** Là một Admin, tôi muốn xem và tìm kiếm danh sách toàn bộ người dùng trong hệ thống.
- *AC:*
  - Hiển thị danh sách phân trang (pagination) người dùng (Họ tên, email, vai trò, trạng thái).
  - Thanh tìm kiếm hoạt động (Search by Name/Email).
  - Khi click vào một user, hiển thị Modal chi tiết.

**Story 4:** Là một Admin, tôi muốn thay đổi quyền hạn (Role) và trạng thái tài khoản.
- *AC:*
  - Có thể khóa (Deactivate) và mở khóa (Activate) tài khoản lập tức.
  - Có thể thăng quyền một User thành Owner hoặc Admin.

#### 3.3. Quản lý Không gian làm việc (Project Management)
**Story 5:** Là một Owner, tôi muốn tạo các dự án khác nhau cho các nhóm khác nhau.
- *AC:*
  - Form tạo dự án yêu cầu Tên (Bắt buộc) và Mô tả.
  - Dự án tạo ra mặc định ở trạng thái ACTIVE.

**Story 6:** Là một Owner, tôi muốn quản lý danh sách các dự án của mình với các bộ lọc.
- *AC:*
  - Hỗ trợ các view: "Tất cả dự án", "Dự án của tôi" (Do mình tạo), "Kho lưu trữ" (Đã archive).
  - Có tính năng ghim (Pin) dự án quan trọng lên đầu trang.

**Story 7:** Là một Owner, tôi muốn thêm và xóa thành viên trong dự án.
- *AC:*
  - Tìm kiếm thành viên bằng email.
  - Hiển thị danh sách thành viên hiện tại.
  - Owner có thể "Kick" thành viên ra khỏi dự án.

#### 3.4. Kho Tài liệu (Document Repository)
**Story 8:** Là một thành viên dự án, tôi muốn upload tài liệu từ máy tính lên không gian dự án.
- *AC:*
  - Hỗ trợ kéo thả (Drag & Drop) trực tiếp vào giao diện.
  - Validate nghiêm ngặt định dạng file: Text (PDF, DOCX, XLSX, PPTX, MD, TXT), Image (JPG, PNG, GIF, SVG, BMP), Video (MP4, MOV, AVI).
  - Cập nhật tiến trình tải lên (Upload progress) trực quan.

**Story 9:** Là một thành viên, tôi muốn quản lý và thao tác với các file đã tải lên.
- *AC:*
  - Danh sách file hiển thị dưới dạng bảng (Tên, Định dạng, Kích thước, Người tải lên, Ngày tải).
  - Có thể xem metadata của file.
  - Nút Download để tải file về máy.
  - Nút Rename để đổi tên file trên hệ thống (không đổi phần mở rộng).
  - Chỉ Owner hoặc người tải lên mới có quyền Delete file.

#### 3.5. Trợ lý AI (Chatbot - Optional Phase 2)
**Story 10:** Là một thành viên, tôi muốn hỏi Chatbot về một quy trình nghiệp vụ đã được lưu trong PDF.
- *AC:*
  - Giao diện chat bên cạnh danh sách tài liệu.
  - Chatbot nhận diện câu hỏi, trích xuất (Retrieval) nội dung từ các file trong dự án đó.
  - Trả lời bằng ngôn ngữ tự nhiên và đính kèm đường link (Reference) trỏ tới trang PDF/phút video chứa câu trả lời.

---

### 4. NFRs (Non-Functional Requirements)

1. **Bảo mật (Security):**
   - Mọi request (trừ login/register) đều phải mang Authorization header chứa JWT Token hợp lệ.
   - Project-level Isolation: User không thuộc Project A sẽ không thể gọi API xem chi tiết hay tải file của Project A dù có JWT hợp lệ.
2. **Hiệu suất (Performance):**
   - Thời gian phản hồi API (không tính upload/download) dưới 200ms.
   - Hỗ trợ phân trang ở mọi list view (Users, Projects, Documents) để tránh tải quá tải DOM và DB.
3. **Tính khả dụng (Usability):**
   - Giao diện (UI) thiết kế theo hướng Component-based, thân thiện, đồng nhất màu sắc (Design System).
   - Responsive hoạt động tốt trên cả Màn hình máy tính (Desktop) và Máy tính bảng (Tablet).
4. **Lưu trữ (Storage):**
   - Sử dụng giải pháp Object Storage (như MinIO hoặc S3) thay vì lưu file trực tiếp trong thư mục source code, đảm bảo khả năng mở rộng không giới hạn (Scalability).

---

### 5. Success Metrics (Tiêu chí Đánh giá Thành công)
- Tỷ lệ lỗi (Error rate) trong quá trình tải file < 1%.
- 100% các thao tác liên quan đến tạo mới/chỉnh sửa (User, Project) phải hiển thị phản hồi thành công (Toast notification).
- Admin dashboard hoạt động trơn tru với dữ liệu giả lập (mock data) lên tới 10,000 bản ghi.

---

### 6. Out of Scope (Ngoài phạm vi hiện tại)
Các tính năng sau đã được ghi nhận nhưng sẽ không phát triển trong Phase 1 (MVP):
- Hệ thống gửi Email Notification tự động (Ví dụ: thông báo khi có file mới).
- Trích xuất chữ viết tay từ hình ảnh (OCR).
- Chỉnh sửa văn bản trực tiếp trên trình duyệt (như Google Docs).
