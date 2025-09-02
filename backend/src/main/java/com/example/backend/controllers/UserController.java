package com.example.backend.controllers;

import com.example.backend.DTOs.UserDTO;
import com.example.backend.exceptions.EmailAlreadyExistsException;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.User;
import com.example.backend.services.UserService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST a new user
    // Endpoint http://localhost:8080/api/users
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

    // GET the current user's data
    // Endpoint http://localhost:8080/api/users/me
    @GetMapping("/me") // get current user's data
    @PreAuthorize("hasRole('USER')")
    public UserDTO getUser(@AuthenticationPrincipal JwtUser jwtUser) throws NotFoundException {
        User user = userService.getUserByEmail(jwtUser.getUsername());
        return new UserDTO(user.getUsername(), user.getEmail(), user.getChats());
    }

    // PUT (update) the current user's profile
    // Endpoint http://localhost:8080/api/users/me
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(@AuthenticationPrincipal JwtUser jwtUser,
                              @RequestBody User newProfile) throws EmailAlreadyExistsException, NotFoundException {
        User user = userService.getUserByEmail(jwtUser.getUsername());
        userService.updateUserProfile(user, newProfile);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
