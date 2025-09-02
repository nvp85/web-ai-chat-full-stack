package com.example.backend.controllers;

import com.example.backend.models.User;
import com.example.backend.services.UserService;
import com.google.genai.Client;
import com.openai.client.OpenAIClient;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    OpenAIClient openAIClient;
    @MockitoBean
    Client googleAIClient;

    @MockitoBean
    UserService userService;

    private User expectedUser;

    @BeforeEach
    void setUp() {
        expectedUser = new User();
        expectedUser.setId(1);
        expectedUser.setEmail("a@b.com");
        expectedUser.setPassword("xS5!234567");
    }


    @Test
    void createUser() throws Exception {
        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "email":"a@b.com","password":"xS5!234567"
                                }
                                """)
                        ).andExpect(status().isCreated());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        User actualUser = userCaptor.getValue();
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getPassword(), actualUser.getPassword());
    }

    @Test
    void createUserShortPass() throws Exception {
        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "email":"a@b.com","password":"xS5!23"
                                }
                                """)
        ).andExpect(status().isBadRequest())
                .andExpect(content().string("Email and password must not be blank and password must be at least 8 characters long"));
    }

    @Test
    void createUserNoEmail() throws Exception {
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                "password":"xS5!234567"
                                }
                                """)
                ).andExpect(status().isBadRequest())
                .andExpect(content().string("Email and password must not be null"));
    }

    @Test
    void getUser() throws Exception {
        when(userService.getUserByEmail("a@b.com"))
                .thenReturn(expectedUser);

        JwtUser jwtUser = new JwtUser();
        jwtUser.setUsername(expectedUser.getEmail());
        jwtUser.setPassword(expectedUser.getPassword());
        jwtUser.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        jwtUser.setAccountNonExpired(true);
        jwtUser.setAccountNonLocked(true);
        jwtUser.setApiAccessAllowed(true);
        jwtUser.setCredentialsNonExpired(true);
        jwtUser.setEnabled(true);

        mockMvc.perform(
                        get("/api/users/me")
                                .with(SecurityMockMvcRequestPostProcessors.user(jwtUser))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    void getUserUnauthenticated() throws Exception {
        mockMvc.perform(
                        get("/api/users/me")
                ).andExpect(status().isUnauthorized());
    }
}