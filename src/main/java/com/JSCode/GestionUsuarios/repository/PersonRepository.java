package com.JSCode.GestionUsuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.JSCode.GestionUsuarios.model.Person;
import com.JSCode.GestionUsuarios.model.User;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByDocument(String Document);
    Optional<Person> findByUser(User user); 
    Optional<Person> findByDocument(String document);

    boolean existsByDocumentAndUserNot(String document, User user);

}
