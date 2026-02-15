package com.secure.notes.controllers;


import com.secure.notes.models.AppRole;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.repositories.UserRepository;
import com.secure.notes.security.jwt.JwtUtils;
import com.secure.notes.security.request.LoginRequest;
import com.secure.notes.security.request.SignupRequest;
import com.secure.notes.security.response.LoginResponse;
import com.secure.notes.security.response.MessageResponse;
import com.secure.notes.security.response.UserInfoResponse;
import com.secure.notes.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    JwtUtils jwtUtils;


    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserService userService;

    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication=authenticationManager
            .authenticate(
                    (new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword())
                    )
            );
        }catch(AuthenticationException e){
            Map<String,Object> map=new HashMap<>();
            map.put("message","Bad credentials");
            map.put("status",false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }
        //set authentication(marking user authenticated in spring security context)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails=(UserDetails) authentication.getPrincipal();
        String jwtToken=null;
        if(userDetails!=null) {
            jwtToken=jwtUtils.generateTokenFromUsername(userDetails);
        }
        //collect roles from userDetails
        List<String>roles=null;
        if(userDetails!=null) {
            roles=userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }
        //prep the response body
        LoginResponse response=null;
        if(userDetails!=null) response=new LoginResponse(userDetails.getUsername(),roles,jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User Already Exists!"));
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email Is Already In Use!"));
        }

        //create user new account
        User user=new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<String>strRoles=signupRequest.getRole();
        Role role;

        if(strRoles==null || strRoles.isEmpty()){
            role=roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(()->new RuntimeException("Error: Role is not found."));
        }else{
            String roleStr=strRoles.iterator().next();
            if(roleStr.equalsIgnoreCase("admin")){
                  return ResponseEntity.badRequest().body(new MessageResponse("Error: Admin registration is not allowed via public API"));
            }else{
                role=roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(()->new RuntimeException("Error: Role is not found."));
            }

        }

        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setCredentialsExpiryDate(LocalDate.now().plusYears(1));
        user.setAccountExpiryDate(LocalDate.now().plusYears(1));
        user.setTwoFactorEnabled(false);
        user.setSignUpMethod("email");
        user.setRole(role);

        try{
            userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Error: Username or Email already exists!"));
        }


        return ResponseEntity.ok(new MessageResponse("User Registered Successfully!"));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails){
        User user=userService.getUserByUsername(userDetails.getUsername());

        List<String> roles=userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse response=new UserInfoResponse(
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
          roles
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/username")
    public String currentUserName(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails!=null?userDetails.getUsername():"";
    }


}




