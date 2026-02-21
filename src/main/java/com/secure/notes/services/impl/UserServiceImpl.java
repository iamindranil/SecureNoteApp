package com.secure.notes.services.impl;

import com.secure.notes.dtos.UpdatePasswordDTO;
import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.AppRole;
import com.secure.notes.models.PasswordResetToken;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.PasswordResetTokenRepository;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.repositories.UserRepository;
import com.secure.notes.services.UserService;
import com.secure.notes.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Value("${frontend.url}")
    String frontendUrl;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    EmailService emailService;

    @Transactional
    @Override
    public void updateUserRole(UUID userId, String roleName) {
        User user=userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        AppRole appRole;
        try{
            appRole=AppRole.valueOf(roleName);
        }catch(IllegalArgumentException e){
            throw new RuntimeException("Invalid role: "+roleName);
        }
        Role role=roleRepository.findByRoleName(appRole).orElseThrow(()->new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user=userRepository.findById(id).orElseThrow(()->new RuntimeException("User not found"));
        return convertToDto(user);
    }

    @Override
    public User getUserByUsername(String username){
        Optional<User>user=userRepository.findByUserName(username);
        return user.orElseThrow(()->new RuntimeException("User not found with username"));
    }

    private UserDTO convertToDto(User user) {
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

    @Transactional
    @Override
    public void updateAccountLockStatus(UUID userId, boolean lock){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setAccountNonLocked(!lock);
        userRepository.save(user);
    }

    @Override
    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }

    @Transactional
    @Override
    public void updateAccountExpiryStatus(UUID userId, boolean expire){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setAccountNonExpired(!expire);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateAccountEnabledStatus(UUID userId, boolean enabled) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateCredentialsExpiryStatus(UUID userId, boolean expire) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setCredentialsNonExpired(!expire);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePassword(UUID userId, String password) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        try{
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }catch(Exception e){
            throw new RuntimeException("Failed to update password");
        }
    }

    @Transactional
    @Override
    public void generatePasswordResetToken(String email){
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found"));
        String token=UUID.randomUUID().toString();
        Instant expiryDate=Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken resetToken=new PasswordResetToken(token,expiryDate,user);
        passwordResetTokenRepository.save(resetToken);
        String resetUrl=frontendUrl+"/reset-password?token="+token;
        //send email to user
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);

    }

    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken=passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()->new RuntimeException("Invalid password-reset token"));
        if(resetToken.isUsed())throw new RuntimeException("reset-token has already been used");
        if(resetToken.getExpiryDate().isBefore(Instant.now()))
            throw new RuntimeException("reset-token has expired");
        User user=resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }


}
