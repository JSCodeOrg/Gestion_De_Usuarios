package com.JSCode.GestionUsuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.model.Roles;

public interface RolesRepository extends JpaRepository<Roles,  Long> {

    Optional<Roles> findByName(String name);


    
}
