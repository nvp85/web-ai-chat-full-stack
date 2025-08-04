package com.example.backend.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.backend.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import com.example.backend.models.User;

import java.util.Collections;

// Custom implementation of UserDetailsService to load user details by email
// This service retrieves user information from the UserRepository and constructs a JwtUser object
// The custom implementation is needed for the Auth library security-jwt
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        JwtUser jwtUser = new JwtUser();
        // jwtUser is a custom implementation of UserDetails
        // it is different from the User model and its username field maps to User.email field
        jwtUser.setUsername(user.getEmail());
        jwtUser.setPassword(user.getPassword());
        // Set authorities to one role: all authenticated users are equal and have the same role
        jwtUser.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        jwtUser.setAccountNonExpired(true);
        jwtUser.setAccountNonLocked(true);
        jwtUser.setApiAccessAllowed(true);
        jwtUser.setCredentialsNonExpired(true);
        jwtUser.setEnabled(true);
        return jwtUser;
    }
}
