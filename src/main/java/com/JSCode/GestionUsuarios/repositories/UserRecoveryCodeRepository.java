package com.JSCode.GestionUsuarios.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserRecoveryCode;

public interface UserRecoveryCodeRepository extends JpaRepository<UserRecoveryCode, Long> {
    Optional<UserRecoveryCode> findByUser(User user);
    void deleteByUser(User user);
}