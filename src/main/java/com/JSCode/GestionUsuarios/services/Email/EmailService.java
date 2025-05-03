package com.JSCode.GestionUsuarios.services.email;

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

    @Value("${frontend.frontendUrl}")
    private String frontendUrl;


    public void sendVerificationEmail(String toEmail, String verificationToken) throws MessagingException {
        String verificationLink = frontendUrl + "/verify?token=" + verificationToken;
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
            message.setSubject("Verifica tu Cuenta");

            String htmlContent = String.format("""
                    <!DOCTYPE html>
                    <html lang='es'>
                    <head>
                      <meta charset='UTF-8'>
                      <style>
                        body {
                          background: #ffffff;
                          font-family: Arial, sans-serif;
                          color: #333;
                          padding: 20px;
                        }
                        .container {
                          max-width: 600px;
                          margin: 0 auto;
                          border: 1px solid #ddd;
                          border-radius: 8px;
                          padding: 30px;
                          box-shadow: 0 0 10px rgba(0,0,0,0.05);
                        }
                        h2 {
                          color: #6a0dad;
                          text-align: center;
                        }
                        .button {
                          display: inline-block;
                          background-color: #6a0dad;
                          color: #fff;
                          padding: 12px 25px;
                          text-decoration: none;
                          border-radius: 5px;
                          margin-top: 20px;
                        }
                        p {
                          line-height: 1.6;
                        }
                        .footer {
                          margin-top: 30px;
                          font-size: 12px;
                          color: #888;
                          text-align: center;
                        }
                      </style>
                    </head>
                    <body>
                      <div class='container'>
                        <h2>Gracias por registrarte</h2>
                        <p>Estamos felices de que estés aquí.</p>
                        <p>Haz clic en el siguiente enlace para verificar tu cuenta:</p>
                        <a href='%s' class='button'>Verificar cuenta</a>
                        <p>Si no te registraste en nuestra plataforma, simplemente ignora este correo.</p>
                        <div class='footer'>© 2025 Shop. Todos los derechos reservados.</div>
                      </div>
                    </body>
                    </html>
                    """, verificationLink);

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar el correo electrónico", e);
        }
    }
}