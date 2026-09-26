package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.UserResponse;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AdminUserService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  // --- Lấy danh sách toàn bộ người dùng (Dành cho Admin) ---
  public Page<UserResponse> list(String search, Pageable pageable) {
    // Nếu có từ khóa tìm kiếm (search), sẽ tìm kiếm cả Email và Họ Tên không phân biệt hoa thường (IgnoreCase)
    if (search != null && !search.trim().isEmpty()) {
      return users.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(search.trim(), search.trim(), pageable).map(UserResponse::from);
    }
    // Nếu không có từ khóa, trả về toàn bộ dữ liệu có phân trang
    return users.findAll(pageable).map(UserResponse::from);
  }

  // --- Admin tạo mới trực tiếp 1 tài khoản ---
  @Transactional
  public UserResponse create(CreateUserRequest request) {
    String email = request.email().trim().toLowerCase();
    
    // Kiểm tra trùng email (Nếu trùng ném ra lỗi HTTP 400)
    if (users.existsByEmail(email)) throw new BadRequestException("Email đã được sử dụng"); // Đã dịch sang Tiếng Việt
    
    User user = new User();
    user.setEmail(email);
    user.setFullName(request.fullName().trim());
    user.setPasswordHash(encoder.encode(request.password())); // Băm mật khẩu (Bcrypt)
    user.setRole(request.role()); // Admin có quyền chọn Role (ADMIN, OWNER, USER) ngay khi tạo
    user.setStatus(UserStatus.ACTIVE); // Mặc định kích hoạt
    return UserResponse.from(users.save(user));
  }

  // --- Thay đổi Quyền hạn (Role) ---
  @Transactional
  public UserResponse changeRole(Long id, UpdateUserRoleRequest request) {
    User user = get(id); // Lấy ra user theo ID
    user.setRole(request.role()); // Ghi đè Role mới
    return UserResponse.from(users.save(user));
  }

  // --- Đảo trạng thái Khóa / Mở Khóa tài khoản ---
  @Transactional
  public UserResponse changeStatus(Long id, UpdateUserStatusRequest request, String adminEmail) {
    User user = get(id);
    
    // Kiểm tra: Không cho phép Admin tự khóa tài khoản của chính mình (Chống "tự sát")
    if (user.getEmail().equalsIgnoreCase(adminEmail) && request.status() == UserStatus.INACTIVE)
      throw new BadRequestException("Bạn không thể tự vô hiệu hóa tài khoản của chính mình");
      
    user.setStatus(request.status());
    return UserResponse.from(users.save(user));
  }

  private User get(Long id) {
    return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
