package com.observatory.observatorysystem.controller;

import com.observatory.observatorysystem.entity.User;
import com.observatory.observatorysystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Аутентификация через Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Устанавливаем аутентификацию в контекст безопасности
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Сохраняем аутентификацию в сессии
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Получаем пользователя из БД
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("role", user.getRole());
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName());
                response.put("userId", user.getId());
                response.put("message", "Добро пожаловать, " + user.getFullName());
                response.put("sessionId", session.getId());

                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("User: " + user.getUsername());
                System.out.println("Role: " + user.getRole());
                System.out.println("Session ID: " + session.getId());
                System.out.println("=====================");

                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("User not found after authentication");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Добавьте это для отладки
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Неверное имя пользователя или пароль");
            return ResponseEntity.status(401).body(response);
        }
    }

    // Дополнительно: регистрация новых пользователей
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь с таким именем уже существует"
            ));
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Пользователь успешно зарегистрирован",
                "userId", savedUser.getId()
        ));
    }

    // Эндпоинт для проверки сессии
    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

        if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            Authentication auth = context.getAuthentication();
            String username = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "username", username,
                    "authorities", authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false
            ));
        }
    }

    // Эндпоинт для выхода
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Вы успешно вышли из системы"
        ));
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}