package com.secure.notes.controllers;


import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.AppRole;
import com.secure.notes.models.Role;
import com.secure.notes.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;


    @GetMapping("/getusers")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PutMapping("/update-role")
    public ResponseEntity<String> updateUserRole(@RequestParam UUID userId, @RequestParam String roleName){
        //invalid roleName check
        try{
            AppRole.valueOf(roleName);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Invalid Role Name: "+roleName);
        }
        userService.updateUserRole(userId,roleName);
        return ResponseEntity.ok("User role has been updated");
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id){
        return new ResponseEntity<>(userService.getUserById(id),HttpStatus.OK);
    }

    @PutMapping("/update-lock-status")
    public ResponseEntity<String> updateAccountLockStatus(@RequestParam UUID userId,
                                                          @RequestParam boolean lock) {
        try{
            userService.updateAccountLockStatus(userId,lock);
            return ResponseEntity.ok("Account lock status updated");
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return userService.getAllRoles();
    }

    @PutMapping("/update-expiry-status")
    public ResponseEntity<String> updateAccountExpiryStatus(@RequestParam UUID userId,
                                                            @RequestParam boolean expire) {
        try {
            userService.updateAccountExpiryStatus(userId, expire);
            return ResponseEntity.ok("Account expiry status updated");
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update-enabled-status")
    public ResponseEntity<String> updateAccountEnabledStatus(@RequestParam UUID userId,
                                                             @RequestParam boolean enabled) {
        try {
            userService.updateAccountEnabledStatus(userId, enabled);
            return ResponseEntity.ok("Account enabled status updated");
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update-credentials-expiry-status")
    public ResponseEntity<String> updateCredentialsExpiryStatus(@RequestParam UUID userId,
                                                                @RequestParam boolean expire) {
        try {
            userService.updateCredentialsExpiryStatus(userId, expire);
            return ResponseEntity.ok("Credentials expiry status updated");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam UUID userId,
                                                 @RequestBody String password) {
        try {
            userService.updatePassword(userId, password);
            return ResponseEntity.ok("Password updated");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }




}
