package com.JSCode.GestionUsuarios.service.email;

import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.regex.Pattern;

@Service
public class checkEmailService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Pattern EMAILPATTERN = Pattern.compile(
        "^[A-Z0-9.%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    );

    public boolean checkEmailExists(String email) {
        if (!isValidEmail(email)) {
            return false;
        }

        String jpql = "SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("email", email);
        Long count = query.getSingleResult();

        return count != null && count > 0;
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAILPATTERN.matcher(email).matches();
    }
}