package com.secure.notes.util;


import com.secure.notes.models.User;
import com.secure.notes.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthUtil {
    @Autowired
    UserRepository userRepository;

    public UUID loggedInUserId(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null||!authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        User user=userRepository.findByUserName(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException("User not found: " + authentication.getName()));
        return user.getUserId();
    }

    public User loggedInUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        assert authentication!=null;
        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(()-> new RuntimeException("user not found"));
    }
}
