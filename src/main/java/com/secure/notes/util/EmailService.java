package com.secure.notes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail,String resetUrl){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Password Reset Request");
        message.setText("Click here to reset password: "+resetUrl);
        try {
            mailSender.send(message);
        }catch(MailException e){
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
