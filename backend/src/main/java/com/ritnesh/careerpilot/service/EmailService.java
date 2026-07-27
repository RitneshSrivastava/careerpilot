package com.ritnesh.careerpilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your CareerPilot verification code by Ritnesh Srivastava");
        message.setText(
                "Your CareerPilot verification code is: " + otpCode +
                        "\n\nThis code expires in 10 minutes.\n\n" +
                        "If you didn't request this, you can ignore this email.\n\n" +
                        "Regards Ritnesh"
        );

        mailSender.send(message);
    }
}
