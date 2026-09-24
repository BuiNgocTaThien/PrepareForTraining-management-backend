# Hướng dẫn dành cho Trợ lý lập trình AI (Gemini Instructions)

Tài liệu này chứa ngữ cảnh chuẩn để cung cấp cho Gemini (hoặc bất kỳ trợ lý lập trình AI nào khác). Nếu bạn là một AI, bạn đang tham gia phát triển dự án cá nhân mang tên **PrepareForTraining Management**. 

**Yêu cầu bắt buộc**: Vui lòng đọc kỹ các file `SRS.md`, `DATABASE_DESIGN.md`, `API_CONTRACT.md` và `IMPLEMENTATION_PLAN.md` trước khi đề xuất bất kỳ thay đổi mã nguồn nào.

## 1. Ràng buộc Kỹ thuật (Technical Constraints)

- **Backend**: Yêu cầu sử dụng Java 21, Spring Boot, Maven, PostgreSQL, Spring Data JPA, Spring Security, JWT và Swagger/OpenAPI.
- **Frontend**: Yêu cầu sử dụng React (Vite), TypeScript, React Router.
- **Quy tắc API**: Tất cả API phải sử dụng tiền tố `/api/v1` và bọc dữ liệu trong class `ApiResponse` dùng chung.
- **Kiến trúc**: Tuyệt đối giữ nguyên sự tách biệt giữa BE và FE. Không bao giờ được trộn lẫn code Frontend vào trong cấu trúc thư mục của Backend.
- **Bảo mật dữ liệu**: Sử dụng DTOs (Data Transfer Objects) làm trung gian giao tiếp. Tuyệt đối KHÔNG trả về thẳng thực thể JPA (JPA entities) từ Controllers ra ngoài.
- **Phân quyền**: Mọi hành động nhạy cảm (thêm, sửa, xoá, xem) đều phải kiểm tra chặt chẽ: (1) Vai trò của user và (2) Quyền sở hữu/thành viên dự án.

## 2. Quy tắc Làm việc (Working Rules)

1. **Tuân thủ đúng giai đoạn (Phase)**: Chỉ lập trình các tính năng thuộc Giai đoạn (Phase) hiện tại đang làm, trừ khi người dùng có yêu cầu cụ thể khác.
2. **Cập nhật tài liệu**: Trước khi thay đổi API (thêm field, đổi đường dẫn), phải cập nhật file `API_CONTRACT.md` và các class/types tương ứng ở Frontend trước.
3. **Mã sạch (Clean Code)**: Trả về mã nguồn dễ đọc, chuẩn xác và không chứa thông tin nhạy cảm (Mật khẩu cứng, Secret keys).
4. **Hướng dẫn rõ ràng**: Sau khi sửa hoặc tạo file, hãy giải thích ngắn gọn lý do và cung cấp lệnh Terminal để người dùng chạy/kiểm thử.
5. **Giới hạn phạm vi**: Hãy hỏi ý kiến người dùng trước khi muốn thêm các dịch vụ nằm ngoài MVP (như Gửi Email, AI, Cloud services...).

## 3. Tình trạng Hiện tại

Hệ thống đã hoàn tất Giai đoạn 1 và 2 (Database, Authentication, Projects, Members) và đã được tích hợp phân trang (Pagination) cùng bộ lọc lỗi chi tiết. Nhiệm vụ hiện tại sắp tới là tiến vào **Giai đoạn 3 (Lưu trữ tài liệu với MinIO/S3)**.
