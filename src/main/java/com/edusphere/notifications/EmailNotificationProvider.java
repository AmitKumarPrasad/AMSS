package com.edusphere.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider {
    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public EmailNotificationProvider(JavaMailSender mailSender,
                                     @Value("${notifications.email.from:no-reply@amss.local}") String from,
                                     @Value("${notifications.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }

    public void send(String recipient, String subject, String body) {
        if (!enabled) throw new IllegalStateException("Email notification provider is disabled");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject == null || subject.isBlank() ? "AMSS notification" : subject);
        message.setText(body);
        mailSender.send(message);
    }
}
