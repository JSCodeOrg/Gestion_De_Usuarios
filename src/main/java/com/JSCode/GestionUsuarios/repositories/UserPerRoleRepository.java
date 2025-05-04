package com.JSCode.GestionUsuarios.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserPerRole;

public interface UserPerRoleRepository extends JpaRepository<UserPerRole, Long> {
    List<UserPerRole> findByUser(User user);
}
