package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.*;
import com.fpt.preparefortraining.dto.response.*;
import com.fpt.preparefortraining.entity.*;
import com.fpt.preparefortraining.exception.*;
import com.fpt.preparefortraining.repository.UserRepository;
import com.fpt.preparefortraining.repository.PasswordResetTokenRepository;
import com.fpt.preparefortraining.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;

  public AuthService(UserRepository users, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
    this.users = users;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.emailService = emailService;
  }

  // --- Chức năng Đăng ký tài khoản (Register) ---
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    
    // 1. Kiểm tra xem email đã tồn tại trong hệ thống chưa, nếu có ném ra lỗi BadRequest
    if (users.existsByEmail(email)) throw new BadRequestException("Email đã được sử dụng");
    
    // 2. Khởi tạo thực thể người dùng (User) mới
    User user = new User();
    user.setEmail(email);
    user.setFullName(request.fullName().trim());
    
    // 3. Băm mật khẩu (Hash password) bằng Bcrypt trước khi lưu vào DB để bảo mật
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER); // Mặc định tài khoản mới luôn là cấp thấp nhất (USER)
    user.setStatus(UserStatus.ACTIVE);
    user = users.save(user); // Lưu vào Database

    // 4. Gửi email chào mừng ngay sau khi đăng ký thành công
    emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

    return authResponse(user); // Sinh ra chuỗi JWT Token và trả về Frontend
  }

  // --- Chức năng Đăng nhập (Login) ---
  public AuthResponse login(LoginRequest request) {
    // 1. Lấy thông tin user trong database qua email
    User user =
        users
            .findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Email hoặc mật khẩu không chính xác"));
            
    // 2. Kiểm tra tài khoản có bị khóa không (UserStatus) VÀ Mật khẩu băm (Hash) có khớp không
    if (user.getStatus() != UserStatus.ACTIVE
        || !passwordEncoder.matches(request.password(), user.getPasswordHash()))
      throw new UnauthorizedException("Email hoặc mật khẩu không chính xác");
      
    // Nếu mọi thứ hợp lệ, trả về JWT Token
    return authResponse(user);
  }

  // --- Đăng nhập bằng Google (Google OAuth2) ---
  @Transactional
  public AuthResponse googleLogin(GoogleLoginRequest request) {
    // 1. Gọi trực tiếp API của Google để xác minh accessToken mà Frontend gửi lên
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(request.accessToken());
    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
    
    ResponseEntity<Map> response;
    try {
        response = restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, entity, Map.class);
    } catch(Exception e) {
        throw new UnauthorizedException("Token Google không hợp lệ");
    }

    Map<String, Object> payload = response.getBody();
    if(payload == null || !payload.containsKey("email")) {
        throw new UnauthorizedException("Token Google không hợp lệ");
    }

    // 2. Trích xuất email và tên thật từ hệ thống của Google
    String email = ((String) payload.get("email")).trim().toLowerCase();
    String name = (String) payload.get("name");
    
    boolean isNewUser = !users.existsByEmail(email);

    // 3. Nếu email chưa tồn tại, tự động tạo tài khoản ngầm cho người dùng
    User user = users.findByEmail(email).orElseGet(() -> {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : "Google User");
        // Sinh ra mật khẩu ngẫu nhiên bằng UUID (Người dùng Google không cần mật khẩu này)
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole(Role.USER);
        newUser.setStatus(UserStatus.ACTIVE);
        return users.save(newUser);
    });

    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new UnauthorizedException("Tài khoản người dùng đã bị vô hiệu hóa");
    }

    if (isNewUser) {
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    return authResponse(user);
  }

  // --- Lấy thông tin user hiện tại (Để giữ phiên đăng nhập) ---
  public UserResponse currentUser(String email) {
    return UserResponse.from(
        users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Không tìm thấy người dùng")));
  }

  // --- Chức năng Quên mật khẩu ---
  @Transactional
  public void forgotPassword(String email) {
      User user = users.findByEmail(email)
              .orElseThrow(() -> new UnauthorizedException("Không tìm thấy người dùng với email " + email));

      if (user.getStatus() != UserStatus.ACTIVE) {
          throw new UnauthorizedException("Tài khoản người dùng đã bị vô hiệu hóa");
      }

      // Xóa tất cả các token đặt lại mật khẩu cũ của user này để tránh lỗi
      passwordResetTokenRepository.deleteByUser(user);

      // Sinh ra 1 chuỗi ngẫu nhiên (UUID) có thời hạn sử dụng là 15 phút
      String token = UUID.randomUUID().toString();
      PasswordResetToken resetToken = new PasswordResetToken();
      resetToken.setToken(token);
      resetToken.setUser(user);
      resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
      passwordResetTokenRepository.save(resetToken); // Lưu token vào bảng password_reset_tokens

      // Gửi đường link nhúng kèm Token tới Email người dùng
      String resetUrl = "http://localhost:5173/reset-password?token=" + token;
      emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);
  }

  // --- Xác nhận mã Token để đổi sang Mật khẩu mới ---
  @Transactional
  public void resetPassword(String token, String newPassword) {
      PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
              .orElseThrow(() -> new UnauthorizedException("Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

      // Kểm tra xem token đã quá 15 phút chưa
      if (resetToken.isExpired()) {
          passwordResetTokenRepository.delete(resetToken);
          throw new UnauthorizedException("Mã đặt lại mật khẩu đã hết hạn");
      }

      // Cập nhật lại mật khẩu mới cho User
      User user = resetToken.getUser();
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      users.save(user);

      // Hủy (Xóa) token này đi để không bị dùng lại lần 2
      passwordResetTokenRepository.delete(resetToken);
  }

  // --- API Cập nhật Thông tin cá nhân (Tên) ---
  @Transactional
  public UserResponse updateProfile(String email, UpdateProfileRequest request) {
    User user = users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Không tìm thấy người dùng"));
    user.setFullName(request.fullName().trim()); // Ghi đè tên mới
    return UserResponse.from(users.save(user));
  }

  // --- API Chủ động đổi Mật khẩu ---
  @Transactional
  public void changePassword(String email, ChangePasswordRequest request) {
    User user = users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Không tìm thấy người dùng"));
    
    // Quan trọng: Phải bắt buộc user nhập đúng mật khẩu cũ mới cho đổi
    if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
      throw new BadRequestException("Mật khẩu cũ không chính xác");
    }
    
    // Băm mật khẩu mới và lưu
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    users.save(user);
  }

  // Hàm private hỗ trợ tạo JWT token để tránh lặp code
  private AuthResponse authResponse(User user) {
    return new AuthResponse(jwtService.generate(user), "Bearer", UserResponse.from(user));
  }
}
