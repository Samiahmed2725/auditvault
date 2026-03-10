package com.auditvault.config;

import com.auditvault.model.User;
import com.auditvault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Always ensure the default auditor exists with the correct password
            User auditor = userRepository.findByEmail("auditor@test.com").orElse(new User());
            
            auditor.setEmail("auditor@test.com");
            String encodedPassword = passwordEncoder.encode("password");
            auditor.setPassword(encodedPassword);
            auditor.setRole(User.Role.CA);
            auditor.setName("Default Auditor");
            auditor.setStatus(User.Status.ACTIVE);
            
            userRepository.save(auditor);
            
            System.out.println("\n=======================================================");
            System.out.println("🚀 [AUDIT VAULT] SYSTEM INITIALIZED SUCCESSFULLY");
            System.out.println("=======================================================");
            System.out.println("👥 DEFAULT AUDITOR ACCOUNT READY");
            System.out.println("📧 Login Email : auditor@test.com");
            System.out.println("🔑 Password    : password");
            System.out.println("=======================================================\n");
        };
    }
}
