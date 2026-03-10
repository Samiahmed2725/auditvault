package com.auditvault.repository;



import com.auditvault.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    java.util.List<Document> findByClient(com.auditvault.model.User client);
}

