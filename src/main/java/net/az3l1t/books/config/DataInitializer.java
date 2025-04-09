package net.az3l1t.books.config;

import net.az3l1t.books.dto.RegisterRequest;
import net.az3l1t.books.model.roles.Role;
import net.az3l1t.books.repository.UserRepository;
import net.az3l1t.books.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, AuthService authService) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                RegisterRequest adminRequest = new RegisterRequest();
                adminRequest.setUsername("admin");
                adminRequest.setPassword("admin");

                authService.registerAdmin(adminRequest);
                System.out.println("Admin user created successfully");
            } else {
                System.out.println("Admin user already exists");
            }
        };
    }
}