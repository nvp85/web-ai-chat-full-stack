package com.example.backend.services;

import com.example.backend.exceptions.EmailAlreadyExistsException;
import com.example.backend.models.User;
import com.example.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createUser() throws Exception {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("xS5!234567");
        when(passwordEncoder.encode("xS5!234567")).thenReturn("hashedPassword");
        when(userRepository.findByEmail("a@b.com")).thenReturn(java.util.Optional.empty());
        userService.createUser(user);
        assertEquals("hashedPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void createUserEmailExists() throws Exception {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("xS5!234567");
        when(userRepository.findByEmail("a@b.com")).thenReturn(java.util.Optional.of(user));
        assertThrows(EmailAlreadyExistsException.class, () -> {userService.createUser(user);});
        verify(userRepository, times(0)).save(user);
    }

    @Test
    void updateUserProfile() throws Exception {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("xS5!234567");
        User newProfile = new User();
        newProfile.setEmail("c@d.org");
        when(userRepository.findByEmail("c@d.org")).thenReturn(java.util.Optional.empty());
        userService.updateUserProfile(user, newProfile);
        assertEquals(user.getEmail(), newProfile.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserEmailExists() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("a@b.com");
        User duplicate = new User();
        duplicate.setId(2);
        duplicate.setEmail("c@d.org");
        User newProfile = new User();
        newProfile.setEmail("c@d.org");
        when(userRepository.findByEmail("c@d.org")).thenReturn(java.util.Optional.of(duplicate));
        assertThrows(EmailAlreadyExistsException.class, () -> {userService.updateUserProfile(user, newProfile);});
        verify(userRepository, times(0)).save(user);
    }

    @Test
    void isPasswordValid() {
        assertTrue(userService.isPasswordValid("xS5!234567"));
        assertFalse(userService.isPasswordValid("shrt1A!"));
        assertFalse(userService.isPasswordValid("nouppercase1!"));
        assertFalse(userService.isPasswordValid("NOLOWERCASE1!"));
        assertFalse(userService.isPasswordValid("NoNumber!"));
        assertFalse(userService.isPasswordValid("NoSpecialChar1"));
    }

    @Test
    void isEmailValid() {
        assertTrue(userService.isEmailValid("a@b.co"));
        assertFalse(userService.isEmailValid("ab.co"));
        assertFalse(userService.isEmailValid("a@bcom"));
        assertFalse(userService.isEmailValid("a@.com"));
    }
}