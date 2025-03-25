package com.JSCode.GestionUsuarios.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.Roles;

public interface RolesRepository extends JpaRepository<Roles,  Long> {

    Optional<Roles> findByName(String name);


    
}
