package com.example.GestionUsuarios.services.Email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

import java.lang.reflect.Field;

import javax.mail.Transport;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.JSCode.GestionUsuarios.services.Email.EmailService;

public class EmailServiceTest {

    private final EmailService emailService = new EmailService();

@Test
    void testSendVerificationEmail_noExceptionThrown() {
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Simula que Transport.send no lanza excepción
            mockedTransport.when(() -> Transport.send(org.mockito.ArgumentMatchers.any()))
                           .thenAnswer(invocation -> null);

            // Configura los campos privados mediante reflexión
            setPrivateField("username", "test@example.com");
            setPrivateField("password", "password123");
            setPrivateField("host", "smtp.example.com");
            setPrivateField("port", "587");
            setPrivateField("frontendUrl", "http://frontend.local");
            setPrivateField("frontendNetUrl", "http://frontendnet.local");

            // Verifica que el método no lanza ninguna excepción
            assertDoesNotThrow(() -> 
                emailService.sendVerificationEmail("user@example.com", "test-token-123")
            );
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }
    private void setPrivateField(String fieldName, String value) throws Exception {
        Field field = emailService.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(emailService, value);
    }
}