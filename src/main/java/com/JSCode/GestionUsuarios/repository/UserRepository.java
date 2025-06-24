package com.JSCode.GestionUsuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByMail(String mail);
    Optional<User> findByMail(String mail);

}
