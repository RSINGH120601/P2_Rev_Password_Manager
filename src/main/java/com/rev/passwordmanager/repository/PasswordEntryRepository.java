package com.rev.passwordmanager.repository;

import com.rev.passwordmanager.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {


    List<PasswordEntry> findByUserId(Long userId);

    Page<PasswordEntry> findByUserId(Long userId, Pageable pageable);

    List<PasswordEntry> findByUserIdAndCategory(Long userId, String category);

    @Query("""
    SELECT p FROM PasswordEntry p
    WHERE p.user.id = :userId
    AND (
        LOWER(p.accountName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.website) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    List<PasswordEntry> searchPasswords(Long userId, String keyword);


}
