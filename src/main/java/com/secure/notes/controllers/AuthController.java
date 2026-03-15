package com.secure.notes.controllers;


import com.secure.notes.dtos.UpdatePasswordDTO;
import com.secure.notes.dtos.UserDTO;
import com.secure.notes.exceptions.ResourceNotFoundException;
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
import com.secure.notes.security.services.UserDetailsImpl;
import com.secure.notes.security.services.UserDetailsServiceImpl;
import com.secure.notes.services.JwtBlacklistService;
import com.secure.notes.services.RateLimitingService;
import com.secure.notes.services.TotpService;
import com.secure.notes.services.UserService;
import com.secure.notes.util.AuthUtil;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    AuthUtil authUtil;

    @Autowired
    TotpService totpService;

    @Autowired
    RateLimitingService rateLimitingService;

    @Autowired
    JwtBlacklistService jwtBlacklistService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication=authenticationManager
                .authenticate(
                        (new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword())
                        )
                );
        //set authentication(marking user authenticated in spring security context)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
        Objects.requireNonNull(userDetails, "User details cannot be null after successful authentication");

        if(userDetails.is2faEnabled()){
            return ResponseEntity.ok(new LoginResponse(userDetails.getUsername(),null,null,true));
        }

        String jwtToken=jwtUtils.generateTokenFromUsername(userDetails);
        //collect roles from userDetails
        List<String>roles=userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        //prep the response body
        LoginResponse response=new LoginResponse(userDetails.getUsername(),roles,jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            throw new IllegalArgumentException("Error: Email is already in use!");
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
                    .orElseThrow(()->new ResourceNotFoundException("Error: Role is not found."));
        }else{
            String roleStr=strRoles.iterator().next();
            if(roleStr.equalsIgnoreCase("admin")){
                throw new IllegalArgumentException("Error: Admin registration is not allowed via public API");
            }else{
                role=roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(()->new ResourceNotFoundException("Error: Role is not found."));
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

        userRepository.save(user);
        //if two person want to register using same email at exact same millisecond...then DataIntegrityViolationException
        //occurs and after global exception refactor it has been handled.
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

    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email){
        userService.generatePasswordResetToken(email);
        return ResponseEntity.ok(new MessageResponse("If this email is registered, a password reset link has been sent."));
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UpdatePasswordDTO newPassword){
        userService.resetPassword(newPassword.getToken(),newPassword.getPassword());
        return ResponseEntity.ok(new MessageResponse("Password reset successful!"));
    }

    //2FA Authentication

    @PostMapping("/enable-2fa")
    public ResponseEntity<String> enable2FA(){
        UUID userId=authUtil.loggedInUserId();
        GoogleAuthenticatorKey secret=userService.generate2FASecret(userId);
        String qrCodeUrl=totpService.getQrCodeUrl(secret,userService.getUserById(userId).getUserName());
        return ResponseEntity.ok(qrCodeUrl);
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<String> disable2FA(){
        UUID userId=authUtil.loggedInUserId();
        userService.disable2FA(userId);
        return ResponseEntity.ok("2FA disabled");
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<String> verify2FA(@RequestParam int code){
        UUID userId=authUtil.loggedInUserId();
        String userName=authUtil.loggedInUser().getUserName();
        //check rate limit
        if(rateLimitingService.isRateLimited(userName)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many failed attempts! Please try again later");
        }
        boolean isValid=userService.validate2FACode(userId,code);
        if(isValid){
            userService.enable2FA(userId);
            //clear rate limit on success
            rateLimitingService.clearRateLimit(userName);
            return ResponseEntity.ok("2FA verified");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid 2FA code");
    }

    @GetMapping("/user/2fa-status")
    public ResponseEntity<?> get2FAStatus(){
        UserDTO user=authUtil.loggedInUser();
        if(user!=null){
            return ResponseEntity.ok().body(Map.of("is2faEnabled",user.isTwoFactorEnabled()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("user not found");
    }

    @PostMapping("/public/verify-2fa-login")
    public ResponseEntity<?>verify2FALogin(@RequestParam int code,@RequestParam String username){

       //String username=jwtUtils.getUserNameFromJwtToken(jwtToken);
        //check rate limit first
        if(rateLimitingService.isRateLimited(username)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many failed attempts. Please try again later.");
        }
        User user=userService.findByUsername(username);
        boolean isValid=userService.validate2FACode(user.getUserId(),code);
        if(isValid){
            //Clear rate limit on success
            rateLimitingService.clearRateLimit(username);
            UserDetailsImpl userDetails=(UserDetailsImpl)userDetailsService.loadUserByUsername(username);
            String jwtToken=jwtUtils.generateTokenFromUsername(userDetails);
            List<String>roles=userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            LoginResponse response=new LoginResponse(userDetails.getUsername(),roles,jwtToken);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid 2FA code");
    }

    @PostMapping("/logout")
    public ResponseEntity<String>logout(HttpServletRequest request){
        String authHeader=request.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String jwt=authHeader.substring(7);
            //Toss it into redis blacklist
            jwtBlacklistService.blacklistToken(jwt);
            return ResponseEntity.ok("Successfully logged out.Token invalidated");
        }
        return ResponseEntity.badRequest().body("No JWT token found in request header");
    }
}




