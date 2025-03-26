package com.JSCode.GestionUsuarios.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByDocument(String Document);

    
}
