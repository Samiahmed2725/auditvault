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
            User auditor = userRepository.findByUserId("auditor123").orElse(new User());

            auditor.setUserId("auditor123");
            auditor.setName("Default Auditor");
            auditor.setRole(User.Role.AUDITOR);
            auditor.setStatus(User.Status.ACTIVE);
            auditor.setPassword(passwordEncoder.encode("password"));

            userRepository.save(auditor);
            
            System.out.println("\n=======================================================");
            System.out.println("🚀 [AUDIT VAULT] SYSTEM INITIALIZED SUCCESSFULLY");
            System.out.println("=======================================================");
            System.out.println("👥 DEFAULT AUDITOR ACCOUNT READY");
            System.out.println("🆔 Login UserId: auditor123");
            System.out.println("🔑 Password    : password");
            System.out.println("=======================================================\n");
        };
    }
}
