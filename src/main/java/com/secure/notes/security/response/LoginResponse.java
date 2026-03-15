package com.secure.notes.security.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private String jwtToken;
    private String username;
    private List<String>roles;
    private boolean is2faRequired;

    public LoginResponse(String username,List<String> roles,String jwtToken){
        this.username=username;
        this.roles=roles;
        this.jwtToken=jwtToken;
    }

    public LoginResponse(String username,List<String> roles,String jwtToken,boolean is2faRequired){
        this.username=username;
        this.roles=roles;
        this.jwtToken=jwtToken;
        this.is2faRequired=is2faRequired;
    }
}
