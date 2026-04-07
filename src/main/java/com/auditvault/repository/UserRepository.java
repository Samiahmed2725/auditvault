package com.auditvault.repository;



import com.auditvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId);
    
    java.util.List<User> findByAuditor(User auditor);
}

