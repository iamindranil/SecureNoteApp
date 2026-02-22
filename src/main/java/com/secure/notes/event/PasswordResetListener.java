package com.secure.notes.event;

import com.secure.notes.util.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PasswordResetListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetEvent(PasswordResetEvent event){
        String resetUrl=frontendUrl+"/reset-password?token="+event.getToken();
        //add try-catch here as due to @async exception thrown inside an @Async method is swallowed.
        try {
            emailService.sendPasswordResetEmail(event.getEmail(), resetUrl);
        }catch(RuntimeException e){
            log.error("Failed to send password reset email to {}", event.getEmail(), e);
            //retry mechanism(future implementation)
        }
    }
}
