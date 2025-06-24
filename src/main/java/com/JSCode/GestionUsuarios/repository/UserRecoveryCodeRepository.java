package com.JSCode.GestionUsuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.model.UserRecoveryCode;

public interface UserRecoveryCodeRepository extends JpaRepository<UserRecoveryCode, Long> {
    Optional<UserRecoveryCode> findByUser(User user);
    void deleteByUser(User user);
}