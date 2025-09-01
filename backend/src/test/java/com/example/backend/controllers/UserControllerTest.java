package com.example.backend.controllers;

import com.example.backend.models.User;
import com.example.backend.services.UserService;
import com.google.genai.Client;
import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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


    @Test
    void createUser() throws Exception {
        User expectedUser = new User();
        expectedUser.setId(1);
        expectedUser.setEmail("a@b.com");
        expectedUser.setPassword("xS5!234567");

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
    void getUser() {

    }

    @Test
    void updateProfile() {
    }
}