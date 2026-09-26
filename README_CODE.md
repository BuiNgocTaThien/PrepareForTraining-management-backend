# Bản đồ Code (Codebase Map) - PrepareForTraining Backend

Chào bạn, đây là tài liệu hướng dẫn nhanh để giúp bạn hiểu và điều hướng dễ dàng trong hệ thống mã nguồn của **Backend Spring Boot**. 

Trong Spring Boot (và Java nói chung), code được chia thành các tầng (Layers) rất rõ ràng. Bạn không cần phải đọc từng file một, chỉ cần nắm rõ vai trò của từng thư mục (Package) là bạn có thể dễ dàng hiểu toàn bộ hệ thống.

---

## 1. Các thư mục cốt lõi đã được Comment 100% tiếng Việt
Đây là những nơi chứa các **Thuật toán, Luồng nghiệp vụ (Business Logic)** và cách Backend nhận yêu cầu từ Frontend. Mình đã comment cực kì chi tiết từng dòng trong các thư mục này:

- 📂 **`com.fpt.preparefortraining.service`** (Tầng Service - Quan trọng nhất)
  - Nơi xử lý toàn bộ não bộ của hệ thống. 
  - Đã comment: `AuthService.java` (Đăng nhập, Quên MK), `ProjectService.java` (Tạo dự án, Phân quyền), `DocumentService.java` (Upload/Download file), `AdminUserService.java` (Tìm kiếm, Khóa user).

- 📂 **`com.fpt.preparefortraining.security`** (Tầng Bảo mật)
  - Nơi chứa logic băm mật khẩu, chặn các yêu cầu không có Token.
  - Đã comment: `JwtService.java` (Sinh token, giải mã token).

- 📂 **`com.fpt.preparefortraining.controller`** (Tầng Giao tiếp)
  - Nơi định nghĩa các đường dẫn API (như `POST /api/v1/auth/login`).
  - Các file ở đây rất mỏng (chỉ 1-2 dòng) vì nó chỉ có nhiệm vụ nhận Request từ React và ném sang cho Tầng `Service` xử lý.
  - Đã được gắn đầy đủ Comment dưới dạng Swagger (`@Operation`) để bạn đọc tài liệu trên giao diện web.

---

## 2. Các thư mục chuẩn mẫu (Boilerplate) - Không cần giải thích dòng
Các thư mục dưới đây chứa các định dạng cấu trúc chuẩn của Java Spring, chúng không chứa "thuật toán" hay "logic phức tạp" nên bạn chỉ cần lướt qua để biết chúng là gì:

- 📂 **`com.fpt.preparefortraining.entity`**
  - Chứa các class đại diện cho **Bảng (Table) trong Database PostgreSQL**.
  - Ví dụ: `User.java` (bảng `users`), `Project.java` (bảng `projects`). 
  - Trong này chỉ có tên biến, cột (như `@Column(name="email")`) chứ không có code chạy.

- 📂 **`com.fpt.preparefortraining.dto`** (Data Transfer Object)
  - Chứa các file định nghĩa **Dữ liệu truyền vào (Request)** và **Dữ liệu trả về (Response)**.
  - Phân nhánh: `dto/request` (những gì React gửi lên, VD: `LoginRequest`) và `dto/response` (những gì Backend trả về, VD: `UserResponse`).
  - Nơi đây chứa các câu báo lỗi bằng tiếng Việt (`@NotBlank(message="Không được bỏ trống")`) mà mình đã thêm hôm nay.

- 📂 **`com.fpt.preparefortraining.repository`**
  - Chứa các class giao tiếp trực tiếp với cơ sở dữ liệu.
  - Spring Boot tự động sinh ra các câu query SQL ở đây. (Ví dụ: `Optional<User> findByEmail(String email);` sẽ tự động biến thành `SELECT * FROM users WHERE email = ?`).

- 📂 **`com.fpt.preparefortraining.exception`**
  - Định nghĩa các "Lỗi tùy chỉnh". (Ví dụ: `BadRequestException` tương đương lỗi `HTTP 400`, `UnauthorizedException` tương đương lỗi `HTTP 401`).
  - Khi Tầng Service gọi `throw new BadRequestException("Sai mật khẩu");`, hệ thống sẽ tự chặn lại và trả về cho React dòng báo lỗi đó màu đỏ.

---
**Tóm lại:** Khi bạn muốn đọc hiểu để thuyết trình hoặc nắm code, bạn chỉ cần mở file trong thư mục **`service`**. Những file khác chỉ mang tính chất định nghĩa cấu trúc!
