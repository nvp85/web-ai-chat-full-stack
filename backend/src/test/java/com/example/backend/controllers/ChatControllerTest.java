package com.example.backend.controllers;

import com.example.backend.DTOs.ChatDTO;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.Chat;
import com.example.backend.models.Message;
import com.example.backend.models.User;
import com.example.backend.services.ChatService;
import com.example.backend.services.UserService;
import com.google.genai.Client;
import com.openai.client.OpenAIClient;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    OpenAIClient openAIClient;

    @MockitoBean
    Client googleAIClient;

    @MockitoBean
    ChatService chatService;

    @MockitoBean
    UserService userService;

    @Test
    void createChat() throws Exception {
        JwtUser jwtUser = Mockito.mock(JwtUser.class);
        when(jwtUser.getUsername()).thenReturn("a@b.com");
        when(jwtUser.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(1);
        user.setEmail("a@b.com");
        when(userService.getUserByEmail("a@b.com")).thenReturn(user);
        when(chatService.createChat(Mockito.any(), Mockito.anyString()))
                .thenAnswer(i -> new ChatDTO(i.getArgument(0), new Message("Response", "assistant")));
        String newChat = """
                {
                    "chat": {
                        "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
                        "title": "Test Chat",
                        "llModel": {
                            "id": 1,
                            "name": "gpt-4o-mini",
                            "provider": "OpenAI"
                        }
                    },
                    "message": {
                        "content": "Hello, world!"
                    }
                }
                """;
        mockMvc.perform(
            post("/api/chats")
                    .with(SecurityMockMvcRequestPostProcessors.user(jwtUser))
                    .contentType("application/json")
                    .content(newChat)
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.chat.title").value("Test Chat"))
        .andExpect(jsonPath("$.message.content").value("Response"));
    }

    @Test
    void updateChatTitle() {

    }

    @Test
    void getChatMessages() {
    }

    @Test
    void promptAndGetResponse() throws Exception {
        JwtUser jwtUser = Mockito.mock(JwtUser.class);
        when(jwtUser.getUsername()).thenReturn("a@b.com");
        when(jwtUser.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(1);
        user.setEmail("a@b.com");
        Chat chat = new Chat();
        chat.setId(java.util.UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851"));
        when(userService.getUserByEmail("a@b.com")).thenReturn(user);
        when(chatService.getChatOrThrow(chat.getId(), "a@b.com")).thenReturn(chat);
        when(chatService.addPromptAndResponse(Mockito.any(Chat.class), Mockito.any(Message.class)))
                .thenAnswer(i -> new ChatDTO(i.getArgument(0), new Message("Response", "assistant")));
        String newMessage = """
                {
                    "content": "Hello, world!"
                }
                """;
        mockMvc.perform(
                post("/api/chats/d290f1ee-6c54-4b01-90e6-d701748f0851/messages")
                        .with(SecurityMockMvcRequestPostProcessors.user(jwtUser)
                        ).contentType("application/json")
                        .content(newMessage)
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.chat.id").value("d290f1ee-6c54-4b01-90e6-d701748f0851"))
        .andExpect(jsonPath("$.message.content").value("Response"));
    }
}