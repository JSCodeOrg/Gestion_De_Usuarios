package com.JSCode.GestionUsuarios.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@Service
public class EmailService {
    
    @Value("${email.username}")
    private String username;
    
    @Value("${email.password}")
    private String password;
    
    @Value("${email.host}")
    private String host;
    
    @Value("${email.port}")
    private String port;
    
    public void sendVerificationEmail(String toEmail, String verificationCode) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Código de verificación para completar tu registro");
            
            String htmlContent = "<h2>Gracias por registrarte</h2>"
                    + "<p>Tu código de verificación es: <strong>" + verificationCode + "</strong></p>"
                    + "<p>Por favor, ingresa este código en nuestra aplicación para completar tu registro.</p>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar el correo electrónico", e);
        }
    }
}