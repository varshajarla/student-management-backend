package com.example.studentmanagement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5175"
})
public class AuthController {

    private final UserAccountRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a student account
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request) {

        if (request.getFullName() == null
                || request.getFullName().isBlank()
                || request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().length() < 8) {

            return respond(
                    HttpStatus.BAD_REQUEST,
                    false,
                    "Enter your name, email, and a password of at least 8 characters."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            return respond(
                    HttpStatus.CONFLICT,
                    false,
                    "This email is already registered. Please log in."
            );
        }

        // Public registration is student-only.
        UserAccount account = new UserAccount();
        account.setFullName(request.getFullName().trim());
        account.setEmail(email);
        account.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        account.setRole("STUDENT");

        userRepository.save(account);

        return respond(
                HttpStatus.CREATED,
                true,
                "Registration successful. You can now log in."
        );
    }

    // Log in a student or an existing faculty account
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request) {

        if (request.getEmail() == null
                || request.getPassword() == null
                || request.getRole() == null
                || request.getEmail().isBlank()
                || request.getPassword().isBlank()
                || request.getRole().isBlank()) {

            return respond(
                    HttpStatus.BAD_REQUEST,
                    false,
                    "Email, password, and role are required."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        Optional<UserAccount> result =
                userRepository.findByEmail(email);

        if (result.isEmpty()) {
            return respond(
                    HttpStatus.UNAUTHORIZED,
                    false,
                    "Invalid email or password."
            );
        }

        UserAccount account = result.get();

        // Passwords must be stored as BCrypt hashes.
        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(
                        request.getPassword(),
                        account.getPasswordHash())) {

            return respond(
                    HttpStatus.UNAUTHORIZED,
                    false,
                    "Invalid email or password."
            );
        }

        String requestedRole = request.getRole().trim();
        String storedRole = account.getRole();

        if (storedRole == null
                || !storedRole.equalsIgnoreCase(requestedRole)) {

            return respond(
                    HttpStatus.UNAUTHORIZED,
                    false,
                    "Invalid email, password, or account type."
            );
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Login successful.");
        body.put("id", account.getId());
        body.put("fullName", account.getFullName());
        body.put("email", account.getEmail());
        body.put("role", storedRole.toUpperCase(Locale.ROOT));

        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> respond(
            HttpStatus status,
            boolean success,
            String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }

    public static class RegisterRequest {
        private String fullName;
        private String email;
        private String password;

        public RegisterRequest() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;
        private String role;

        public LoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}