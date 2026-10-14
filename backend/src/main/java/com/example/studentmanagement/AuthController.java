package com.example.studentmanagement;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})
public class AuthController {

    private final UserAccountRepository repository;

    public AuthController(UserAccountRepository repository) {
        this.repository = repository;
    }

    // STUDENT REGISTRATION
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> data) {

        String fullName = data.get("fullName");
        String email = data.get("email");
        String password = data.get("password");
        String requestedRole = data.get("role");

        if (fullName == null || email == null || password == null
                || fullName.isBlank() || email.isBlank()
                || password.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Please fill in all required fields"));
        }

        email = email.trim().toLowerCase();

        if (repository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "This email is already registered"));
        }

        /*
         * Public registration creates STUDENT accounts only.
         * Faculty accounts must be created or approved separately.
         */
        if (requestedRole != null
                && requestedRole.trim().equalsIgnoreCase("FACULTY")) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("message",
                            "Faculty registration requires administrator approval"));
        }

        UserAccount user = new UserAccount();
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setPasswordHash(
                BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole("STUDENT");

        UserAccount saved = repository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Registration successful",
                        "id", saved.getId(),
                        "fullName", saved.getFullName(),
                        "email", saved.getEmail(),
                        "role", saved.getRole()
                ));
    }

    // STUDENT AND FACULTY LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> data) {

        String email = data.get("email");
        String password = data.get("password");
        String role = data.get("role");

        if (email == null || password == null || role == null
                || email.isBlank() || password.isBlank()
                || role.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Email, password, and account type are required"));
        }

        email = email.trim().toLowerCase();
        role = role.trim().toUpperCase();

        Optional<UserAccount> result = repository.findByEmail(email);

        if (result.isEmpty()) {
            return invalidLogin();
        }

        UserAccount user = result.get();

        if (user.getRole() == null
                || !role.equalsIgnoreCase(user.getRole().trim())) {
            return invalidLogin();
        }

        String storedPassword = user.getPasswordHash();

        if (storedPassword == null || storedPassword.isBlank()) {
            return invalidLogin();
        }

        boolean passwordMatches;

        if (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {

            passwordMatches = BCrypt.checkpw(password, storedPassword);

        } else {
            // Support accounts created with plain-text passwords previously.
            passwordMatches = password.equals(storedPassword);

            if (passwordMatches) {
                user.setPasswordHash(
                        BCrypt.hashpw(password, BCrypt.gensalt()));
                repository.save(user);
            }
        }

        if (!passwordMatches) {
            return invalidLogin();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ));
    }

    private ResponseEntity<?> invalidLogin() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message",
                        "Invalid email, password, or account type"));
    }
}