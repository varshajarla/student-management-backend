package com.example.studentmanagement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createFaculty(
            UserAccountRepository repository) {

        return args -> {
            String email = "faculty@gmail.com";

            if (repository.findByEmail(email).isEmpty()) {
                UserAccount faculty = new UserAccount();

                faculty.setFullName("Faculty Admin");
                faculty.setEmail(email);
                faculty.setPasswordHash(
                    new BCryptPasswordEncoder()
                        .encode("Faculty@123")
                );
                faculty.setRole("FACULTY");

                repository.save(faculty);

                System.out.println(
                    "Faculty account created successfully."
                );
            }
        };
    }
}