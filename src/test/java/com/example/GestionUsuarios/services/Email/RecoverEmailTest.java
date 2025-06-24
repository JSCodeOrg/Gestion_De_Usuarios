package com.example.GestionUsuarios.services.Email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

import java.lang.reflect.Field;

import javax.mail.Transport;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.JSCode.GestionUsuarios.service.email.RecoverEmail;

public class RecoverEmailTest {

    private final RecoverEmail recoverEmail = new RecoverEmail();

@Test
    void testSendRecoverEmail_noExceptionThrown() {
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(org.mockito.ArgumentMatchers.any()))
                           .thenAnswer(invocation -> null);

            setPrivateField("username", "test@example.com");
            setPrivateField("password", "password123");
            setPrivateField("host", "smtp.example.com");
            setPrivateField("port", "587");

            assertDoesNotThrow(() ->
                recoverEmail.sendRecoverEmail("user@example.com", "123456")
            );
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }
  @Test
    void testSendLoginData_noExceptionThrown() {
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(org.mockito.ArgumentMatchers.any()))
                           .thenAnswer(invocation -> null);

            setPrivateField("username", "test@example.com");
            setPrivateField("password", "password123");
            setPrivateField("host", "smtp.example.com");
            setPrivateField("port", "587");

            assertDoesNotThrow(() ->
                recoverEmail.sendLoginData("user@example.com", "provisional123")
            );
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    private void setPrivateField(String fieldName, String value) throws Exception {
        Field field = recoverEmail.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(recoverEmail, value);
    }
}