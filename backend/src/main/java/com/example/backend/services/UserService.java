package com.example.backend.services;

import com.example.backend.exceptions.EmailAlreadyExistsException;
import com.example.backend.models.User;
import com.example.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(User user) throws EmailAlreadyExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        if (!isEmailValid(user.getEmail()) || !isPasswordValid(user.getPassword())) {
            throw  new IllegalArgumentException("Please provide valid email and password.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void updateUserProfile(User user, User newProfile) throws EmailAlreadyExistsException {
        if (newProfile.getUsername() != null) {
            user.setUsername(newProfile.getUsername());
        }
        if (newProfile.getEmail() != null && !isEmailValid(newProfile.getEmail()) ) {
            throw  new IllegalArgumentException("Please provide valid email.");
        }
        // if the user wants to change the email there shouldn't be any duplicates
        if (newProfile.getEmail() != null && !newProfile.getEmail().equals(user.getEmail())) {
            Optional<User> duplicate = userRepository.findByEmail(newProfile.getEmail());
            if (duplicate.isPresent() && duplicate.get().getId() != user.getId()) {
                throw new EmailAlreadyExistsException();
            }
            user.setEmail(newProfile.getEmail());
        }
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public boolean isPasswordValid(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$";
        return password.matches(pattern);
    }

    public boolean isEmailValid(String email) {
        String pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
    }
}
