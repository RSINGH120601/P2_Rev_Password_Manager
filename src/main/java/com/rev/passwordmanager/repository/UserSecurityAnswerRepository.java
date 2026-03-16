package com.rev.passwordmanager.repository;

import com.rev.passwordmanager.entity.UserSecurityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSecurityAnswerRepository
        extends JpaRepository<UserSecurityAnswer, Long> {

    Optional<UserSecurityAnswer>
    findByUser_IdAndSecurityQuestion_Id(Long userId, Long questionId);

    List<UserSecurityAnswer> findByUser_IdOrderByIdAsc(Long userId);
}