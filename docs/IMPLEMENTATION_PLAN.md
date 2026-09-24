# Kế hoạch Triển khai (Implementation Plan)

Quy trình phát triển dự án này được chia thành 5 giai đoạn chính. Yêu cầu hoàn thành triệt để từng tính năng trước khi chuyển sang giai đoạn mới.

## Giai đoạn 0 — Nền tảng (Foundation)

- `[x]` Cài đặt môi trường cơ bản: Java 21, Maven, Node.js LTS, PostgreSQL, và IDE (IntelliJ/VSCode).
- `[x]` Cấu hình database PostgreSQL ở môi trường local (file `application-local.yml`).
- `[x]` Chạy thành công API Health-check của Backend và trang chủ của Frontend React.

## Giai đoạn 1 — Mô hình Dữ liệu và Xác thực (Phase 1)

- `[x]` Tạo các Entities cơ bản, Repositories, kịch bản Flyway (Migrations) và các hằng số phân quyền (Role Enums).
- `[x]` Cài đặt logic Đăng ký/Đăng nhập, mã hoá mật khẩu BCrypt, bộ lọc bảo mật JWT (JWT Filter) và cấu hình Spring Security.
- `[x]` Thêm API `GET /users/me`, cài đặt Xử lý lỗi toàn cục (`GlobalExceptionHandler`), và viết Unit Tests.
- `[x]` *(Đã tối ưu)*: Cải tiến Exception Handler để trả về lỗi Validation chi tiết.

## Giai đoạn 2 — Dự án và Quản lý thành viên (Phase 2)

- `[x]` Xây dựng logic CRUD (Tạo, Đọc, Sửa, Lưu trữ) cho Dự án và kiểm tra chặt chẽ quyền Owner.
- `[x]` Cài đặt logic thêm/xoá/xem danh sách thành viên dự án (Bảo vệ trùng lặp thành viên).
- `[x]` *(Đã tối ưu)*: Áp dụng Phân trang (Pagination) cho toàn bộ API lấy danh sách.
- `[x]` Xây dựng các trang giao diện (FE): Đăng nhập, Đăng ký, Bảng điều khiển (Dashboard), Chi tiết dự án.

## Giai đoạn 3 — Lưu trữ Tài liệu (Phase 3)

- `[x]` Tạo lớp trừu tượng hoá cho lưu trữ: `FileStorageService`.
- `[x]` Cài đặt cơ chế lưu trữ trên ổ đĩa máy chủ cục bộ (Local Storage) trước, sau đó nâng cấp lên MinIO/S3 Object Storage.
- `[x]` Xây dựng các endpoints: Tải lên (Upload), Danh sách (List), Tải xuống (Download) và Xoá (Delete). Cài đặt bài test bảo mật.
- `[x]` Xây dựng giao diện Frontend: Trình duyệt danh sách tài liệu và Giao diện kéo thả (Drag & Drop) để upload.

## Giai đoạn 4 — Chất lượng và Bàn giao (Phase 4)

- `[x]` Hoàn thiện toàn bộ API Document bằng Swagger/OpenAPI. Cập nhật Hướng dẫn chạy dự án vào file README.
- `[ ]` Kiểm thử thực tế các luồng Phân quyền (Authorization) xen kẽ giữa các tài khoản Admin, Owner và User.


---

### Tiêu chuẩn Hoàn thành (Definition of Done) cho mỗi tính năng:
Một API/Chức năng chỉ được xem là **Hoàn thành** khi nó: Đã kiểm tra đầu vào (Validated), đã chặn quyền truy cập trái phép (Authorized), được mô tả đầy đủ trong Swagger, đã vượt qua khâu kiểm thử thủ công và Frontend đã bao phủ toàn bộ các trạng thái: Đang tải (Loading), Lỗi (Error), Trống (Empty) và Thành công (Success).
