package com.auditvault.controller;

import com.auditvault.model.Document;
import com.auditvault.model.User;
import com.auditvault.service.DocumentService;
import com.auditvault.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("clientId") Long clientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") com.auditvault.model.DocumentType docType,
            @RequestParam("financialYear") String financialYear
    ) throws IOException {

        User auditor = getAuthenticatedUser();
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Document document = documentService.uploadDocument(file, client, auditor, docType, financialYear);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/list/{clientId}")
    public List<Document> listDocuments(@PathVariable Long clientId) {
        User requestingUser = getAuthenticatedUser();
        System.out.println("📂 API: List Documents | Requestor: " + requestingUser.getEmail() + " (ID: " + requestingUser.getId() + ") | Target Client ID: " + clientId);

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<Document> docs = documentService.getDocumentsByClient(client, requestingUser);
        System.out.println("✅ Found " + docs.size() + " documents for Client ID " + clientId);
        return docs;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        User auditor = getAuthenticatedUser();
        // This still checks access rights
        Document document = documentService.getDocument(id, auditor);
        
        try {
            byte[] fileData = documentService.downloadDocumentBytesFromSupabase(document.getFilePath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFilePath() + "\"")
                    .body(fileData);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file from Supabase: " + document.getFilePath(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        User auditor = getAuthenticatedUser();
        documentService.deleteDocument(id, auditor);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
