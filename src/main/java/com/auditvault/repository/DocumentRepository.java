package com.auditvault.repository;



import com.auditvault.model.Document;
import com.auditvault.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    java.util.List<Document> findByClient(com.auditvault.model.User client);

    List<Document> findByClientAndFinancialYear(com.auditvault.model.User client, String financialYear);

    List<Document> findByClientAndDocumentType(com.auditvault.model.User client, DocumentType documentType);

    List<Document> findByClientAndFinancialYearAndDocumentType(com.auditvault.model.User client, String financialYear, DocumentType documentType);
}

