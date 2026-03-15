package com.secure.notes.security.services;

import com.secure.notes.exceptions.ResourceNotFoundException;
import com.secure.notes.models.User;
import com.secure.notes.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    @Cacheable(value="userDetails",key="#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(()->new ResourceNotFoundException("User Not Found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}
