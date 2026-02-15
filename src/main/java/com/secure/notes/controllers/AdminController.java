package com.secure.notes.controllers;


import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.AppRole;
import com.secure.notes.models.User;
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
    public ResponseEntity<List<User>> getAllUsers(){
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




}
