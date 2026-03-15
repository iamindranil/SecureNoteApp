package com.secure.notes.util;


import com.secure.notes.dtos.UserDTO;
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

    public UserDTO loggedInUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        assert authentication!=null;
        User user=userRepository.findByUserName(authentication.getName())
                .orElseThrow(()-> new RuntimeException("user not found"));
        return new UserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getCredentialsExpiryDate(),
                user.getAccountExpiryDate(),
                user.isTwoFactorEnabled(),
                user.getSignUpMethod(),
                user.getRole(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }
}
