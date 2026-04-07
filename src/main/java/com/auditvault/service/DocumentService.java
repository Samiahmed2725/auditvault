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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public DocumentService(DocumentRepository documentRepository, AuditService auditService) {
        this.documentRepository = documentRepository;
        this.auditService = auditService;
        this.restTemplate = new RestTemplate();
    }

    public Document uploadDocument(MultipartFile file, User client, User uploadedBy, DocumentType docType, String financialYear) throws IOException {
        checkAccess(client, uploadedBy);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
        String storedPath;

        // If Supabase isn't configured locally, fall back to local disk storage for reliable local testing
        boolean supabaseConfigured = supabaseUrl != null && !supabaseUrl.isBlank()
                && supabaseKey != null && !supabaseKey.isBlank()
                && supabaseBucket != null && !supabaseBucket.isBlank();

        if (!supabaseConfigured) {
            try {
                Path uploadsDir = Paths.get("uploads");
                Files.createDirectories(uploadsDir);
                Path dest = uploadsDir.resolve(fileName);
                Files.write(dest, file.getBytes());
                storedPath = "local/" + fileName;
                System.out.println("📦 Stored locally: " + dest.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("❌ Local file write failed: " + e.getMessage());
                throw new RuntimeException("Upload failed: could not store file locally", e);
            }
        } else {
            String storageUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.valueOf(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            System.out.println("🚀 Uploading to Supabase bucket: " + supabaseBucket + " | File: " + fileName);

            try {
                ResponseEntity<String> response = restTemplate.exchange(storageUrl, HttpMethod.POST, requestEntity, String.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Supabase upload failed: " + response.getStatusCode() + " " + response.getBody());
                }
            } catch (Exception e) {
                System.err.println("❌ Supabase upload failed: " + e.getMessage());
                throw new RuntimeException("Upload failed: Supabase error", e);
            }

            storedPath = fileName;
        }

        Document doc = new Document();
        doc.setClient(client);
        doc.setUploadedBy(uploadedBy);
        doc.setDocumentType(docType);
        doc.setFinancialYear(financialYear);
        // We store the simple filename in the DB. We construct the download URL dynamically later.
        doc.setFilePath(storedPath);
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

    public List<Document> getDocumentsByClient(User client, User requestingUser, String financialYear, DocumentType docType) {
        checkAccess(client, requestingUser);

        boolean hasYear = financialYear != null && !financialYear.isBlank();
        boolean hasType = docType != null;

        if (hasYear && hasType) {
            return documentRepository.findByClientAndFinancialYearAndDocumentType(client, financialYear, docType);
        }
        if (hasYear) {
            return documentRepository.findByClientAndFinancialYear(client, financialYear);
        }
        if (hasType) {
            return documentRepository.findByClientAndDocumentType(client, docType);
        }
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
        if (fileName != null && fileName.startsWith("local/")) {
            try {
                String localName = fileName.substring("local/".length());
                Path p = Paths.get("uploads").resolve(localName);
                return Files.readAllBytes(p);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read local file", e);
            }
        }

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
        String currentUserId = requestingUser.getUserId();
        String role = requestingUser.getRole() == null ? "null" : requestingUser.getRole().name();
        String clientUserId = client.getUserId();
        String clientAuditorUserId = (client.getAuditor() == null) ? null : client.getAuditor().getUserId();

        System.out.println("🔐 Doc access check: currentUserId=" + currentUserId
                + " role=" + role
                + " clientDbId=" + client.getId()
                + " clientUserId=" + clientUserId
                + " clientAuditorUserId=" + clientAuditorUserId);

        if (requestingUser.getRole() == User.Role.AUDITOR) {
            if (client.getAuditor() == null || client.getAuditor().getUserId() == null) {
                throw new org.springframework.security.access.AccessDeniedException("Access Denied: client has no assigned auditor.");
            }
            if (!client.getAuditor().getUserId().equals(currentUserId)) {
                throw new org.springframework.security.access.AccessDeniedException("Access Denied: you do not manage this client.");
            }
            return;
        }

        if (requestingUser.getRole() == User.Role.CLIENT) {
            if (clientUserId == null || !clientUserId.equals(currentUserId)) {
                throw new org.springframework.security.access.AccessDeniedException("Access Denied: not your client account.");
            }
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException("Access Denied: unsupported role.");
    }

    private void checkAccessForDocument(Document doc, User user) {
        checkAccess(doc.getClient(), user);
    }

    public void deleteDocument(Long docId, User auditor) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        checkAccess(doc.getClient(), auditor);
        if (auditor.getRole() != User.Role.AUDITOR) {
            throw new org.springframework.security.access.AccessDeniedException("Only auditors can delete documents");
        }
        
        // Delete from storage (Supabase or local)
        try {
            if (doc.getFilePath() != null && doc.getFilePath().startsWith("local/")) {
                String localName = doc.getFilePath().substring("local/".length());
                Path p = Paths.get("uploads").resolve(localName);
                Files.deleteIfExists(p);
                System.out.println("🗑️ Deleted local file: " + p.toAbsolutePath());
            } else {
                String deleteUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + doc.getFilePath();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(supabaseKey);
                headers.set("apikey", supabaseKey);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
                System.out.println("🗑️ Deleted file from Supabase: " + doc.getFilePath());
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to delete stored file: " + e.getMessage());
        }

        documentRepository.delete(doc);
        
        auditService.logAction(auditor, com.auditvault.model.AuditLog.Action.DELETE_DOCUMENT, 
             "File: " + doc.getFilePath() + " | DocId: " + docId);
    }
}
