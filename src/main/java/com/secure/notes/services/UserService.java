package com.secure.notes.services;


import com.secure.notes.dtos.UpdatePasswordDTO;
import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    void updateUserRole(UUID userId, String roleName);

    List<UserDTO> getAllUsers();

    UserDTO getUserById(UUID id);

    User getUserByUsername(String username);

    void updateAccountLockStatus(UUID userId, boolean lock);

    List<Role> getAllRoles();

    void updateAccountExpiryStatus(UUID userId, boolean expire);

    void updateAccountEnabledStatus(UUID userId, boolean enabled);

    void updateCredentialsExpiryStatus(UUID userId, boolean expire);

    void updatePassword(UUID userId, String password);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

}
