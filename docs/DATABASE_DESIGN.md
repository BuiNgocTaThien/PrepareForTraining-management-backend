# Thiết kế Cơ sở dữ liệu (Database Design)

## 1. Danh sách các Bảng (Tables)

| Tên Bảng (Table) | Các cột quan trọng (Columns) | Ghi chú |
| --- | --- | --- |
| **users** | `id`, `email`, `password_hash`, `full_name`, `role`, `status`, `created_at` | Lưu trữ thông tin người dùng. Cột `email` là duy nhất (UNIQUE) dùng để đăng nhập. |
| **projects** | `id`, `name`, `description`, `owner_id`, `status`, `created_at`, `updated_at` | Thông tin dự án. Cột `owner_id` là khoá ngoại (Foreign Key) trỏ tới bảng `users`. |
| **project_members** | `id`, `project_id`, `user_id`, `joined_at` | Bảng trung gian kết nối người dùng vào dự án. Cặp `(project_id, user_id)` là duy nhất để tránh việc một người tham gia 2 lần vào cùng 1 dự án. |
| **documents** | `id`, `project_id`, `uploaded_by`, `original_name`, `object_key`, `content_type`, `size_bytes`, `created_at` | Lưu trữ siêu dữ liệu tài liệu. Cột `object_key` là đường dẫn vật lý trỏ tới file được lưu trong MinIO hoặc ổ cứng. |

## 2. Mối quan hệ giữa các Bảng (Relationships)

*   **User (1) — (N) Project:** Một người dùng (User) có thể là Chủ sở hữu (Owner) của nhiều Dự án. Liên kết qua cột `projects.owner_id`.
*   **User (N) — (N) Project:** Mối quan hệ nhiều-nhiều. Một người dùng có thể tham gia nhiều Dự án, và một Dự án có thể có nhiều Thành viên. Được thể hiện qua bảng trung gian `project_members`.
*   **Project (1) — (N) Document:** Một Dự án chứa nhiều Tài liệu. 
*   **User (1) — (N) Document:** Một Người dùng có thể tải lên nhiều Tài liệu (lưu lại vết qua cột `uploaded_by`).

## 3. Các Enum (Danh mục trạng thái)

- `Role` (Quyền): `ADMIN`, `OWNER`, `USER`
- `ProjectStatus` (Trạng thái Dự án): `ACTIVE` (Đang hoạt động), `ARCHIVED` (Đã lưu trữ)
- `UserStatus` (Trạng thái Người dùng): `ACTIVE` (Đang hoạt động), `INACTIVE` (Đã bị vô hiệu hoá)
