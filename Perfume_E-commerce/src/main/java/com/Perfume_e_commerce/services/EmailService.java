package com.Perfume_e_commerce.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Perfume Shop Verification Code");
        message.setText("Welcome! Your verification code is: " + code + "\n\nThis code expires in 5 minutes.");

        mailSender.send(message);
    }
}
