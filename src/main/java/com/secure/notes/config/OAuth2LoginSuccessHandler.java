package com.secure.notes.config;

import com.secure.notes.models.AppRole;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.security.jwt.JwtUtils;
import com.secure.notes.security.services.UserDetailsImpl;
import com.secure.notes.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    private final UserService userService;

    @Autowired
    private final JwtUtils jwtUtils;

    @Autowired
    RoleRepository roleRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    String username;
    String idAttributeKey;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)throws ServletException, IOException{
        OAuth2AuthenticationToken oAuth2AuthenticationToken=(OAuth2AuthenticationToken) authentication;
        if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())
                ||
           "google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())
        ){
            DefaultOAuth2User principal=(DefaultOAuth2User) authentication.getPrincipal();
            assert principal!=null;
            Map<String,Object>attributes=principal.getAttributes();
            String email=(String) attributes.get("email");
            String name=(String) attributes.get("name");
            if(email==null||name==null){
                throw new RuntimeException("Error while fetching email and name");
            }
            if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
                username=attributes.getOrDefault("login","").toString();
                idAttributeKey="id";
            }else if("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
                username=email.split("@")[0];
                idAttributeKey="sub";
            }else{
                username="";
                idAttributeKey="id";
            }

            userService.findByEmail(email)
                    .ifPresentOrElse(user->{
                        DefaultOAuth2User oAuth2User=new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                                attributes,
                                idAttributeKey
                        );
                        Authentication securityAuth=new OAuth2AuthenticationToken(
                                oAuth2User,
                                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    },()->{
                        User newUser=new User();
                        Optional<Role> userRole=roleRepository.findByRoleName(AppRole.ROLE_USER);
                        if(userRole.isPresent()){
                            newUser.setRole(userRole.get());//set existing role
                        }else{
                            throw new RuntimeException("Default Role not found");
                        }
                        newUser.setEmail(email);
                        newUser.setUserName(username);
                        newUser.setSignUpMethod(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        userService.registerUser(newUser);//save in DB
                        DefaultOAuth2User oAuth2User=new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())),
                                attributes,
                                idAttributeKey
                        );
                        Authentication securityAuth=new OAuth2AuthenticationToken(
                                oAuth2User,
                                List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                       });
        }
        this.setAlwaysUseDefaultTargetUrl(true);

        //JWT Token Logic
        DefaultOAuth2User oAuth2User=(DefaultOAuth2User) authentication.getPrincipal();
        assert oAuth2User!=null;
        Map<String,Object>attributes=oAuth2User.getAttributes();

        //Extract necessary attributes
        String email=(String)attributes.get("email");

        Set<SimpleGrantedAuthority>authorities=oAuth2User.getAuthorities().stream()
                .map(authority ->
                        new SimpleGrantedAuthority(Objects.requireNonNull(authority.getAuthority())))
                .collect(Collectors.toSet());

        User user=userService.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName().name()));


        //create UserDetailsImpl instance
        UserDetailsImpl userDetails=new UserDetailsImpl(
                null,
                username,
                email,
                null,
                false,
                authorities
        );
        //generate JWT Token
        String jwtToken=jwtUtils.generateTokenFromUsername(userDetails);
        //redirect to the frontend with JWT token
        String targetUrl= UriComponentsBuilder.fromUriString(frontendUrl+"/oauth2/redirect")
                .queryParam("token",jwtToken)
                .build()
                .toUriString();
        this.setDefaultTargetUrl(targetUrl);
        super.onAuthenticationSuccess(request,response,authentication);
    }
}
