package com.fpt.preparefortraining.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fpt.preparefortraining.entity.Role;
import com.fpt.preparefortraining.entity.User;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void generatesAndValidatesTokenForUserEmail() {
        JwtService service = new JwtService("test-secret-key-with-at-least-thirty-two-characters", 60_000);
        User user = new User();
        user.setEmail("member@example.com");
        user.setRole(Role.USER);
        String token = service.generate(user);
        assertEquals("member@example.com", service.extractEmail(token));
        assertTrue(service.isValid(token, "member@example.com"));
    }
}
