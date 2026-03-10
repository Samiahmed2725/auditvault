package com.auditvault.service;

import com.auditvault.model.Document;
import com.auditvault.model.DocumentType;
import com.auditvault.model.User;
import com.auditvault.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ClientService clientService;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public DocumentService(DocumentRepository documentRepository, ClientService clientService, AuditService auditService) {
        this.documentRepository = documentRepository;
        this.clientService = clientService;
        this.auditService = auditService;
        this.restTemplate = new RestTemplate();
    }

    public Document uploadDocument(MultipartFile file, User client, User uploadedBy, DocumentType docType, String financialYear) throws IOException {
        checkAccess(client, uploadedBy);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
        String storageUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.valueOf(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        System.out.println("🚀 Uploading to Supabase bucket: " + supabaseBucket + " | File: " + fileName);
        
        ResponseEntity<String> response = restTemplate.exchange(storageUrl, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload file to Supabase: " + response.getBody());
        }

        Document doc = new Document();
        doc.setClient(client);
        doc.setUploadedBy(uploadedBy);
        doc.setDocumentType(docType);
        doc.setFinancialYear(financialYear);
        // We store the simple filename in the DB. We construct the download URL dynamically later.
        doc.setFilePath(fileName);
        doc.setUploadedAt(LocalDateTime.now());

        Document savedDoc = documentRepository.save(doc);
        
        auditService.logAction(uploadedBy, com.auditvault.model.AuditLog.Action.UPLOAD, 
            "File: " + fileName + " | ClientId: " + client.getId());
            
        return savedDoc;
    }

    public List<Document> getDocumentsByClient(User client, User requestingUser) {
        checkAccess(client, requestingUser);
        return documentRepository.findByClient(client);
    }
    
    public Document getDocument(Long id, User requestingUser) {
        Document doc = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        checkAccessForDocument(doc, requestingUser);
        
        auditService.logAction(requestingUser, com.auditvault.model.AuditLog.Action.DOWNLOAD, 
             "File: " + doc.getFilePath() + " | DocId: " + doc.getId());

        return doc;
    }
    
    // New method to fetch the file bytes directly from Supabase for the Controller to return
    public byte[] downloadDocumentBytesFromSupabase(String fileName) {
        String downloadUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.set("apikey", supabaseKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, byte[].class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to download file from Supabase");
        }
    }

    private void checkAccess(User client, User requestingUser) {
        if (requestingUser.getId().equals(client.getId())) {
            return;
        }

        if (client.getAuditor() == null || !client.getAuditor().getId().equals(requestingUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied: You do not have permission to access this client's documents.");
        }
    }

    private void checkAccessForDocument(Document doc, User user) {
        boolean isAuditor = user.getRole() == User.Role.CA;
        boolean isOwnerClient = user.getRole() == User.Role.CLIENT && doc.getClient().getId().equals(user.getId());
        
        if (isAuditor) {
            if (doc.getClient().getAuditor() == null || !doc.getClient().getAuditor().getId().equals(user.getId())) {
                throw new RuntimeException("Access Denied: You do not manage this client.");
            }
        } else if (!isOwnerClient) {
             throw new RuntimeException("Access Denied: Not your document.");
        }
    }

    public void deleteDocument(Long docId, User auditor) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (auditor.getRole() != User.Role.CA || doc.getClient().getAuditor() == null || !doc.getClient().getAuditor().getId().equals(auditor.getId())) {
             throw new RuntimeException("Access Denied: You do not manage this client or are not authorized to delete.");
        }
        
        // Delete from Supabase
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + doc.getFilePath();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.set("apikey", supabaseKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
            System.out.println("🗑️ Deleted file from Supabase: " + doc.getFilePath());
        } catch (Exception e) {
            System.err.println("Warning: Failed to delete file from Supabase: " + e.getMessage());
        }

        documentRepository.delete(doc);
        
        auditService.logAction(auditor, com.auditvault.model.AuditLog.Action.DELETE, 
             "File: " + doc.getFilePath() + " | DocId: " + docId);
    }
}
