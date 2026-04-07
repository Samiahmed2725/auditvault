package com.auditvault.service;

import com.auditvault.model.User;
import com.auditvault.repository.DocumentRepository;
import com.auditvault.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final AuditService auditService;

    public ClientService(UserRepository userRepository, PasswordEncoder passwordEncoder, DocumentRepository documentRepository, DocumentService documentService, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.auditService = auditService;
    }

    public User createClient(User client, User auditor) {
        client.setAuditor(auditor);
        client.setRole(User.Role.CLIENT);
        if (client.getUserId() == null || client.getUserId().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "userId is required");
        }
        String incomingUserId = client.getUserId();
        String cleanUserId = incomingUserId.trim().toLowerCase();
        System.out.println("👤 Creating client. incomingUserId=" + incomingUserId + " cleanUserId=" + cleanUserId);

        if (userRepository.findByUserId(cleanUserId).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "User ID already exists");
        }
        if (client.getPassword() == null || client.getPassword().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "password is required");
        }
        if (client.getName() == null || client.getName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "name is required");
        }
        client.setUserId(cleanUserId);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        client.setStatus(User.Status.ACTIVE); // Default active
        return userRepository.save(client);
    }

    public List<User> getClientsByAuditor(User auditor) {
        return userRepository.findByAuditor(auditor);
    }

    public void deleteClient(Long clientId, User auditor) {
        if (auditor.getRole() != User.Role.AUDITOR) {
            throw new org.springframework.security.access.AccessDeniedException("Only auditors can delete clients");
        }

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getAuditor() == null || !client.getAuditor().getId().equals(auditor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not manage this client");
        }

        // Safely delete related documents first (including Supabase objects)
        java.util.List<com.auditvault.model.Document> docs = documentRepository.findByClient(client);
        for (com.auditvault.model.Document doc : docs) {
            documentService.deleteDocument(doc.getId(), auditor);
        }

        userRepository.delete(client);

        auditService.logAction(auditor, com.auditvault.model.AuditLog.Action.DELETE_CLIENT,
                "Client deleted: dbId=" + client.getId() + " userId=" + client.getUserId());
    }

    public User updateClientEmail(Long clientDbId, String email, User auditor) {
        if (auditor.getRole() != User.Role.AUDITOR) {
            throw new org.springframework.security.access.AccessDeniedException("Only auditors can update client email");
        }

        User client = userRepository.findById(clientDbId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getAuditor() == null || !client.getAuditor().getId().equals(auditor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not manage this client");
        }

        String normalized = email == null ? null : email.trim();
        if (normalized != null && normalized.isBlank()) {
            normalized = null;
        }
        if (normalized != null && !normalized.contains("@")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid email");
        }

        client.setEmail(normalized);
        return userRepository.save(client);
    }
}
