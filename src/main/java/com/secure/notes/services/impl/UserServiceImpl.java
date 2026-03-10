package com.secure.notes.services.impl;


import com.secure.notes.dtos.UserDTO;
import com.secure.notes.event.PasswordResetEvent;
import com.secure.notes.models.AppRole;
import com.secure.notes.models.PasswordResetToken;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.PasswordResetTokenRepository;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.repositories.UserRepository;
import com.secure.notes.services.TotpService;
import com.secure.notes.services.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    TotpService totpService;

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
        Optional<User> userOpt=userRepository.findByEmail(email);
        if(userOpt.isEmpty())return;
        User user=userOpt.get();
        String token=UUID.randomUUID().toString();
        Instant expiryDate=Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetToken resetToken=new PasswordResetToken(token,expiryDate,user);
        passwordResetTokenRepository.deleteByUser(user);//delete old token
        passwordResetTokenRepository.save(resetToken);
        //Publish event for email sending
        eventPublisher.publishEvent(new PasswordResetEvent(user.getEmail(),token));
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

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void registerUser(User user){
        if(user.getPassword()!=null){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        convertToDto(user);
    }

    @Override
    public GoogleAuthenticatorKey generate2FASecret(UUID userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("user not found"));
        GoogleAuthenticatorKey key= totpService.generateSecret();
        user.setTwoFactorSecret(key.getKey());
        userRepository.save(user);
        return key;
    }

    @Override
    public boolean validate2FACode(UUID userId, int code){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("user not found"));
        return totpService.verifyCode(user.getTwoFactorSecret(),code);
    }

    @Override
    public void enable2FA(UUID userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("user not found"));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disable2FA(UUID userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("user not found"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(()-> new RuntimeException("user not found"));
    }
}
