package com.JSCode.GestionUsuarios.services.Email;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Service
public class checkEmailService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        try {
            String jpql = "SELECT COUNT(u) > 0 FROM User u WHERE u.mail = :email";
            TypedQuery<Boolean> query = entityManager.createQuery(jpql, Boolean.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (Exception e) {
            // Loggear el error
            throw new RuntimeException("Error al verificar el email en la base de datos", e);
        }
    }
}