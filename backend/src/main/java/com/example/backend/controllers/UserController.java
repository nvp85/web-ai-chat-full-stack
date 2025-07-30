package com.example.backend.controllers;

import com.example.backend.DTOs.UserDTO;
import com.example.backend.exceptions.EmailAlreadyExistsException;
import com.example.backend.models.User;
import com.example.backend.services.UserService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody User user) throws EmailAlreadyExistsException {
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        } else if (user.getEmail().isEmpty() || user.getPassword().length() <= 8) {
            throw new IllegalArgumentException("Email and password must not be blank and password must be at least 8 characters long");
        }
        userService.createUser(user);
    }

    @GetMapping("/me") // get current user's data
    @PreAuthorize("hasRole('ROLE_USER')")
    public UserDTO getUser(@AuthenticationPrincipal JwtUser jwtUser) {
        User user = userService.getUserByEmail(jwtUser.getUsername());
        return new UserDTO(user.getUsername(), user.getEmail(), user.getChats());
    }

    @PutMapping("/me") // update user's profile
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(@AuthenticationPrincipal JwtUser jwtUser, @RequestBody User newProfile) {
        User user = userService.getUserByEmail(jwtUser.getUsername());
        userService.updateUserProfile(user, newProfile);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')") // TODO: remove this endpoint later
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
