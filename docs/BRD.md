# Business Requirements Document (BRD)
## PrepareForTraining (KBase - Knowledge Base)

---

### 1. Executive Summary (Tóm tắt dự án)
**PrepareForTraining (KBase)** là một giải pháp chuyển đổi số (Digital Transformation) trong việc quản lý tri thức nội bộ. Trong bối cảnh các doanh nghiệp, tổ chức và nhóm làm việc (teams) đang phải đối mặt với tình trạng phân tán thông tin qua email, ổ cứng cá nhân và các ứng dụng chat, KBase ra đời để cung cấp một không gian lưu trữ (Workspace) an toàn, tập trung và thông minh. Dự án đặc biệt chú trọng vào việc bảo vệ dữ liệu dự án và tương lai sẽ tích hợp AI để tối ưu hóa thời gian tìm kiếm thông tin của nhân viên.

### 2. Business Objectives & Drivers (Mục tiêu kinh doanh)

#### 2.1. Động lực kinh doanh (Business Drivers)
- **Thất thoát tri thức**: Khó khăn trong việc bàn giao tài liệu khi có nhân sự nghỉ việc hoặc chuyển bộ phận.
- **Tốn thời gian**: Nhân viên tốn trung bình 20% thời gian làm việc chỉ để tìm kiếm tài liệu và thông tin.
- **Rủi ro bảo mật**: Tài liệu mật bị chia sẻ vô tội vạ ra các nền tảng public (Google Drive cá nhân, Zalo).

#### 2.2. Mục tiêu đo lường (Business Objectives)
- **O1**: Số hoá 100% quy trình lưu trữ tài liệu dự án lên một nền tảng duy nhất.
- **O2**: Rút ngắn thời gian tra cứu tài liệu từ mức trung bình 15 phút xuống còn dưới 1 phút.
- **O3**: Đảm bảo tuân thủ bảo mật, 100% tài liệu chỉ được truy cập bởi nhân sự có thẩm quyền (Project Members).

---

### 3. Scope of Work (Phạm vi dự án)

#### 3.1. In-Scope (Trong phạm vi triển khai MVP)
- **Hệ thống định danh**: Đăng ký, đăng nhập, phân quyền (Role-based Access Control - RBAC).
- **Quản lý không gian làm việc (Projects)**: Tạo dự án, đóng băng (archive) dự án, quản lý thành viên.
- **Quản lý tài liệu (Documents)**: Upload file lớn, phân loại định dạng, tải file gốc về máy.
- **Tích hợp Cloud Storage**: Cơ sở hạ tầng linh hoạt hỗ trợ MinIO hoặc AWS S3.

#### 3.2. Out-of-Scope (Ngoài phạm vi MVP)
- Gửi Email tự động (Auto-notifications, Marketing emails).
- Trình soạn thảo văn bản trực tuyến (Online Editor) giống Google Docs.
- Tính năng AI Chatbot đọc file PDF/Video (Đưa vào lộ trình Phase 2).

---

### 4. Stakeholder Matrix (Ma trận các bên liên quan)

| Bên liên quan (Stakeholder) | Vai trò trong dự án | Mức độ ảnh hưởng (Influence) | Sự quan tâm (Interest) |
| --- | --- | --- | --- |
| **Ban Giám đốc (Sponsors)** | Người ra quyết định và cấp vốn đầu tư. | Cao | Cao |
| **Quản trị viên (Admin)** | Người duy trì hệ thống, quản lý vận hành nền tảng. | Cao | Trung bình |
| **Chủ dự án (Project Owners)**| Nhóm khách hàng chính tạo ra dữ liệu, mua gói dịch vụ. | Trung bình | Cao |
| **Nhân viên (Users)** | Nhóm người dùng cuối, thường xuyên thao tác hệ thống. | Thấp | Cao |

---

### 5. High-level Business Requirements

#### BR-01: Quản trị Hệ thống Tập trung
Hệ thống phải cung cấp một màn hình quản trị duy nhất cho phép Admin xem mọi hoạt động liên quan đến người dùng, từ việc khởi tạo, cấp quyền đến việc khóa tài khoản khi có sự cố.

#### BR-02: Tạo không gian làm việc an toàn
Người quản lý (Owner) phải có quyền tự chủ trong việc tạo Không gian làm việc (Projects) và chỉ định ai được phép tham gia. Các dự án phải hoàn toàn độc lập với nhau về mặt dữ liệu.

#### BR-03: Tính tương thích định dạng tệp tin
Hệ thống phải chấp nhận toàn bộ các định dạng file phục vụ công việc văn phòng và multimedia (Văn bản, Bảng tính, Slide trình chiếu, Hình ảnh, Video) để không làm gián đoạn thói quen làm việc của User.

---

### 6. Risks & Mitigations (Rủi ro & Kế hoạch giảm thiểu)

| Rủi ro (Risk) | Xác suất | Tác động | Kế hoạch giảm thiểu (Mitigation) |
| --- | --- | --- | --- |
| Dữ liệu người dùng bị rò rỉ | Thấp | Cao | Áp dụng chuẩn mã hóa Bcrypt cho password, bảo vệ API bằng JWT Token và check Role ở tầng Service. |
| Quá tải server khi nhiều người tải file video | Trung bình | Trung bình | Tách bạch Database (Postgres) và Storage (MinIO). Thiết lập File Size Limit cho mỗi upload. |
| User gặp khó khăn khi làm quen hệ thống | Thấp | Thấp | Thiết kế UI/UX tinh gọn, sử dụng Alert/Toast để hướng dẫn từng bước. |
