package com.example.studentmanagement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Configuration
public class FacultyPasswordReset {

    @Bean
    CommandLineRunner resetFacultyPassword(
            UserAccountRepository repository) {

        return args -> repository.findByEmail("faculty@gmail.com")
            .ifPresent(user -> {
                user.setPasswordHash(
                    BCrypt.hashpw("Faculty@12345", BCrypt.gensalt())
                );
                user.setRole("FACULTY");
                repository.save(user);
                System.out.println(
                    "Faculty password reset completed."
                );
            });
    }
}