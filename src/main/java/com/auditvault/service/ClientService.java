package com.auditvault.service;

import com.auditvault.model.User;
import com.auditvault.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createClient(User client, User auditor) {
        client.setAuditor(auditor);
        client.setRole(User.Role.CLIENT);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        client.setStatus(User.Status.ACTIVE); // Default active
        return userRepository.save(client);
    }

    public List<User> getClientsByAuditor(User auditor) {
        return userRepository.findByAuditor(auditor);
    }
}
