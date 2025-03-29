package com.JSCode.GestionUsuarios.services.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@Service
public class RecoverEmail {

    @Value("${email.username}")
    private String username;
    
    @Value("${email.password}")
    private String password;
    
    @Value("${email.host}")
    private String host;
    
    @Value("${email.port}")
    private String port;


    public void sendRecoverEmail(String toEmail) throws MessagingException {
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
            message.setSubject("Instrucciones de restablecimineto enviadas a tu correo electronico");
            
            String htmlContent = "<h2>Restablecimiento de contraseña</h2>"
                    + "<p>Hemos recibido una solicitud para restablecer tu contraseña.</p>"
                    + "<p>Para continuar, haz clic en el siguiente enlace:</p>"
                    + "<p><a href='enlace' style='font-size: 16px; color: #007BFF;'>Restablecer contraseña</a></p>"
                    + "<p>Si no solicitaste este cambio, puedes ignorar este mensaje. Tu contraseña actual seguirá siendo la misma.</p>"
                    + "<p>Atentamente,</p>"
                    + "<p><strong>El equipo de soporte</strong></p>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar el correo electrónico de restablecimiento", e);
        }
    }
    
}
