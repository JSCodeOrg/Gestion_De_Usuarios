package com.JSCode.GestionUsuarios.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByMail(String mail);
    Optional<User> findByMail(String mail);

}
