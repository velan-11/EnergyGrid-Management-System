package com.cts.identity_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends transactional emails such as the password-reset OTP. */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(
            String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("EnergyGrid Password Reset OTP");

        message.setText(
                "Hello,\n\n" +
                        "You requested a password reset for your EnergyGrid account.\n\n" +
                        "Your OTP is: " + otp + "\n\n" +
                        "This OTP will expire in 1 minute.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "EnergyGrid Team"
        );

        mailSender.send(message);
    }
}
