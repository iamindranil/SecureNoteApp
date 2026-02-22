package com.secure.notes.event;

import com.secure.notes.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class PasswordResetListener {
    @Value("${frontend.url}")
    String frontendUrl;

    @Autowired
    EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetEvent(PasswordResetEvent event){
        String resetUrl=frontendUrl+"/reset-password?token="+event.getToken();
        emailService.sendPasswordResetEmail(event.getEmail(), resetUrl);
    }
}
