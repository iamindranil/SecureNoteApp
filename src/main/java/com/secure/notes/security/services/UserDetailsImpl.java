package com.secure.notes.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.secure.notes.models.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class UserDetailsImpl implements UserDetails, Serializable {
    private static final long serialVersionUID=1L;

    private UUID id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private boolean is2faEnabled;

    private Collection<? extends GrantedAuthority> autorities;

    public UserDetailsImpl(UUID id,String username,String email,String password,boolean is2faEnabled,Collection<? extends GrantedAuthority> autorities){
        this.id=id;
        this.username=username;
        this.email=email;
        this.password=password;
        this.is2faEnabled=is2faEnabled;
        this.autorities=autorities;
    }

    public static UserDetailsImpl build(User user){
        List<GrantedAuthority> authorities;
        if(user.getRole()!=null && user.getRole().getRoleName()!=null){
            authorities=List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name()));
        }else{
            authorities=List.of();
        }
        return new UserDetailsImpl(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                user.isTwoFactorEnabled(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return autorities;
    }


    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
