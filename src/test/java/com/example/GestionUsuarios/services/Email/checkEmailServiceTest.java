package com.example.GestionUsuarios.services.Email;

import com.JSCode.GestionUsuarios.services.Email.checkEmailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class checkEmailServiceTest {

    private EntityManager mockEntityManager;
    private TypedQuery<Long> mockQuery;
    private checkEmailService service;

    @BeforeEach
    void setUp() throws Exception {
        mockEntityManager = mock(EntityManager.class);
        mockQuery = mock(TypedQuery.class);
        service = new checkEmailService();

        // Inyectar EntityManager por reflexión
        Field emField = checkEmailService.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(service, mockEntityManager);

        // Comportamiento común para los tests
        when(mockEntityManager.createQuery(anyString(), eq(Long.class))).thenReturn(mockQuery);
    }

    @Test
    void testEmailValidoExiste() {
        String email = "test@example.com";

        when(mockQuery.setParameter(eq("email"), eq(email))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(1L);

        boolean result = service.checkEmailExists(email);
        assertTrue(result);
    }

    @Test
    void testEmailValidoNoExiste() {
        String email = "notfound@example.com";

        when(mockQuery.setParameter(eq("email"), eq(email))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(0L);

        boolean result = service.checkEmailExists(email);
        assertFalse(result);
    }

    @Test
    void testEmailInvalido() {
        String email = "correo_invalido";

        boolean result = service.checkEmailExists(email);
        assertFalse(result);

        // Asegurar que no se llamó a EntityManager si el email no es válido
        verify(mockEntityManager, never()).createQuery(anyString(), eq(Long.class));
    }
}
