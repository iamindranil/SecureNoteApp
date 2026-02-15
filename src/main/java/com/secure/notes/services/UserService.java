package com.secure.notes.services;


import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    void updateUserRole(UUID userId, String roleName);

    List<UserDTO> getAllUsers();

    UserDTO getUserById(UUID id);

    User getUserByUsername(String username);
}
