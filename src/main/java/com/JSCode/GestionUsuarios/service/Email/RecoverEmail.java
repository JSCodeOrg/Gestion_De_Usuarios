package com.JSCode.GestionUsuarios.service.Email;

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


    public void sendRecoverEmail(String toEmail, String verificationCode) throws MessagingException {
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
            message.setSubject("JSCode - Reestablecimiento de contraseña");
            
            String htmlContent = "<h2>Restablecimiento de contraseña</h2>"
                    + "<p>Hola</p>"
                    + "<p>Hemos recibido una solicitud para restablecer tu contraseña.</p>"
                    + "<p>Tu código de recuperación es: <strong>" + verificationCode + "</strong></p>"
                    + "<p>Por favor, ingresa este código en nuestra aplicación para continuar con el proceso de restablecimiento.</p>"
                    + "<p>Si no solicitaste este cambio, puedes ignorar este mensaje. Tu contraseña actual seguirá siendo la misma.</p>"
                    + "<p>Atentamente,</p>"
                    + "<p><strong>El equipo de soporte</strong></p>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar el correo electrónico de restablecimiento", e);
        }
    }

    public void sendLoginData(String toEmail, String provitional_password) throws MessagingException {
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
            message.setSubject("JSCode - Credenciales de Acceso");
            
            String htmlContent = "<html>"
            + "<head>"
            + "<style>"
            + "body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f4f4f9; color: #333; }"
            + "h2 { color: #4CAF50; }"
            + "p { margin-bottom: 15px; }"
            + "strong { color: #333; }"
            + ".important { color: #d9534f; font-weight: bold; }"
            + "</style>"
            + "</head>"
            + "<body>"
            + "<h2>Información de Acceso</h2>"
            + "<p>Hola! <strong>Bienvenido a nuestro equipo JSCode</strong>, es un honor trabajar contigo.</p>"
            + "<p>Te damos acceso a la plataforma con los siguientes datos provisionales:</p>"
            + "<p><strong>Tu contraseña es: </strong><span class='important'>" + provitional_password + "</span></p>"
            + "<p><strong>Ojo:</strong> Recuerda cambiar la información de tu cuenta inmediatamente después de entrar.</p>"
            + "<p>No des esta información a nadie, ya que podría usarse para suplantar tu identidad.</p>"
            + "<p>Si no solicitaste acceso a la plataforma, por favor, <strong>no lo compartas</strong>.</p>"
            + "<p>Atentamente,</p>"
            + "<p><strong>El equipo de soporte JSCode</strong></p>"
            + "</body>"
            + "</html>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar el correo electrónico de restablecimiento", e);
        }
    }
    
}
