package com.mohamedoujdid.annotationplatform.notification;

import com.mohamedoujdid.annotationplatform.exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendTemporaryCredentials(String toEmail, String tempPassword) {
        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);

        String content = templateEngine.process("email-templates/temp-credentials", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(toEmail);
            helper.setSubject("Your Temporary Credentials");
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email", e);
        }
    }
}
