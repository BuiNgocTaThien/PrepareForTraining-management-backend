package com.fpt.preparefortraining.security;

import com.fpt.preparefortraining.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret, // Lấy secret key từ file application.properties
      @Value("${app.jwt.expiration-ms}") long expirationMs) { // Lấy thời gian hết hạn (ms)
    
    // Thuật toán HS256 yêu cầu khóa bí mật phải dài ít nhất 32 ký tự
    if (secret.length() < 32)
      throw new IllegalArgumentException("JWT secret must have at least 32 characters");
      
    // Mã hóa chuỗi secret thành đối tượng SecretKey
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  // --- Sinh ra chuỗi Token khi User đăng nhập thành công ---
  public String generate(User user) {
    var now = new Date();
    return Jwts.builder()
        .subject(user.getEmail()) // Dữ liệu chính là Email của user
        .claim("role", user.getRole().name()) // Đính kèm thêm Role (ADMIN/OWNER/USER) vào token
        .issuedAt(now) // Thời gian phát hành
        .expiration(new Date(now.getTime() + expirationMs)) // Thời gian hết hạn
        .signWith(key) // Ký điện tử bằng Secret Key
        .compact(); // Nén lại thành chuỗi chuỗi JWT (Header.Payload.Signature)
  }

  // --- Đọc Email từ Token ---
  public String extractEmail(String token) {
    return claims(token).getSubject(); // Trích xuất subject (chính là Email)
  }

  // --- Xác minh tính hợp lệ của Token ---
  public boolean isValid(String token, String email) {
    // 1. Email trong token phải khớp với email truyền vào
    // 2. Token chưa bị hết hạn (thời gian hết hạn phải sau thời điểm hiện tại)
    return email.equals(extractEmail(token)) && claims(token).getExpiration().after(new Date());
  }

  // --- Hàm giải mã và xác minh chữ ký ---
  private Claims claims(String token) {
    // Nếu token bị giả mạo (sửa payload) thì chữ ký sẽ sai và hàm verifyWith(key) sẽ ném ra lỗi (Exception)
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
