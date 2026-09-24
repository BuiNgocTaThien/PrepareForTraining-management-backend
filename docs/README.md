# Tài liệu hệ thống PrepareForTraining Management

Chào mừng đến với hệ thống Quản lý cơ sở tri thức Dự án (PrepareForTraining Management). Trước khi bắt đầu lập trình hoặc tích hợp bất kỳ module nào, vui lòng đọc tuần tự các tài liệu sau đây để hiểu rõ bức tranh tổng thể:

1. `SRS.md` — Đặc tả yêu cầu phần mềm: Hiểu rõ phạm vi dự án và các tính năng bắt buộc.
2. `DATABASE_DESIGN.md` — Thiết kế cơ sở dữ liệu: Nắm bắt các Bảng, Trường dữ liệu và Mối quan hệ thực thể.
3. `API_CONTRACT.md` — Hợp đồng API: Các quy chuẩn về đường dẫn (Endpoint), quyền truy cập và dữ liệu trao đổi (Request/Response).
4. `IMPLEMENTATION_PLAN.md` — Kế hoạch triển khai: Lộ trình phát triển hệ thống qua từng giai đoạn và định nghĩa thế nào là "Hoàn thành" (Definition of Done).
5. `GEMINI_INSTRUCTIONS.md` — Bộ quy tắc và ngữ cảnh an toàn dành riêng cho các Trợ lý Lập trình AI (Coding Assistants) khi tham gia vào dự án.

## Hướng dẫn Chạy Dự án (Run Instructions)

Để khởi chạy toàn bộ hệ thống ở môi trường local, bạn cần thực hiện tuần tự 3 bước sau:

**Bước 1: Khởi động Database & MinIO (Docker)**
Mở Terminal tại thư mục `backend` và chạy:
```bash
docker-compose up -d
```
Lệnh này sẽ khởi động PostgreSQL (cổng 5432) và MinIO (cổng 9000 & 9001).

**Bước 2: Khởi động Backend (Spring Boot)**
Vẫn ở Terminal thư mục `backend`, chạy lệnh Maven:
```bash
mvn spring-boot:run
```
Hệ thống sẽ tự động tạo bảng (qua Flyway) và Backend sẽ chạy ở cổng `8080`.
Bạn có thể xem tài liệu API Swagger tại: http://localhost:8080/swagger-ui.html

**Bước 3: Khởi động Frontend (React/Vite)**
Mở Terminal mới tại thư mục `frontend` và chạy:
```bash
npm install
npm run dev
```
Giao diện web sẽ chạy ở cổng `5173`. Truy cập http://localhost:5173 để sử dụng!
