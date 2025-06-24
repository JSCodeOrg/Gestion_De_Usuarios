package com.JSCode.GestionUsuarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.model.UserPerRole;

public interface UserPerRoleRepository extends JpaRepository<UserPerRole, Long> {
    List<UserPerRole> findByUser(User user);
    List<UserPerRole> findAllByUser(User user);
}
