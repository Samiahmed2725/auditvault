package com.auditvault.controller;

import com.auditvault.model.User;
import com.auditvault.service.ClientService;
import com.auditvault.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final UserRepository userRepository;

    public ClientController(ClientService clientService, UserRepository userRepository) {
        this.clientService = clientService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public User createClient(@RequestBody User client) {
        User auditor = getAuthenticatedUser();
        return clientService.createClient(client, auditor);
    }

    @GetMapping
    public List<User> getClients() {
        User auditor = getAuthenticatedUser();
        return clientService.getClientsByAuditor(auditor);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
