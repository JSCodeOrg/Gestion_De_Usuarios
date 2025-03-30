package com.JSCode.GestionUsuarios.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByDocument(String Document);
    Optional<Person> findByUser(User user); 

    boolean existsByDocumentAndUserNot(String document, User user);

}
