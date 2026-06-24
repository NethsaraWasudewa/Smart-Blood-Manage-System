package user;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailEngine {

    // --- YOUR GMAIL AND 16-DIGIT APP PASSWORD ---
    private static final String SENDER_EMAIL = "mybloodbankproject";
    private static final String APP_PASSWORD = "ilib lpoz kpvz xqzz"; 

    public static void sendEmailInBackground(String recipientEmail, String subject, String htmlBody) {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            // FIXED: Forces modern TLS protocol so Google doesn't silently reject the login
            props.put("mail.smtp.ssl.protocols", "TLSv1.2"); 
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, "Smart Blood Bank System"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("Automated email successfully sent to: " + recipientEmail);

            } catch (Exception e) {
                System.err.println("CRITICAL EMAIL FAILURE to: " + recipientEmail);
                e.printStackTrace();
            }
        }).start();
    }
}