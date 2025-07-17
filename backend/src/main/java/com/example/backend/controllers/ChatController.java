package com.example.backend.controllers;

import com.example.backend.DTOs.ChatCreationDTO;
import com.example.backend.models.Chat;
import com.example.backend.models.Message;
import com.example.backend.models.User;
import com.example.backend.services.ChatService;
import com.example.backend.services.UserService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('ROLE_USER')")
@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping
    public List<Chat> getAllChats(@AuthenticationPrincipal JwtUser jwtUser) {
        // jwtUser username is the email of the user
        return chatService.getAllChats(jwtUser.getUsername());
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message createChat(@AuthenticationPrincipal JwtUser jwtUser, @RequestBody ChatCreationDTO chatCreationDTO) {
        // ChatCreationDTO contains the chat object and the first prompt
        User user = userService.getUserByEmail(jwtUser.getUsername());
        Chat chat = chatCreationDTO.getChat();
        chat.setUser(user);
        return chatService.createChat(chat, chatCreationDTO.getFirstPrompt());
    }

    @GetMapping("/{chatId}")
    public Chat getChatById(@AuthenticationPrincipal JwtUser jwtUser, @PathVariable UUID chatId) {
        // validate ownership
        Chat chat = chatService.getChatById(chatId);
        if (!jwtUser.getUsername().equals(chat.getUser().getEmail())) {
            throw new AccessDeniedException("You do not have permission to access this chat");
        }
        return chat;
    }

    @GetMapping("/{chatId}/messages")
    public List<Message> getChatMessages(@AuthenticationPrincipal JwtUser jwtUser, @PathVariable UUID chatId) {
        // validate ownership
        Chat chat = chatService.getChatById(chatId);
        if (!jwtUser.getUsername().equals(chat.getUser().getEmail())) {
            throw new AccessDeniedException("You do not have permission to access this chat");
        }
        return chat.getMessages();
    }

    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message promptAndGetResponse(@AuthenticationPrincipal JwtUser jwtUser, @PathVariable UUID chatId, @RequestBody Message prompt) {
        // validate ownership
        Chat chat = chatService.getChatById(chatId);
        if (!jwtUser.getUsername().equals(chat.getUser().getEmail())) {
            // spring security will handle this exception
            throw new AccessDeniedException("You do not have permission to access this chat");
        }
        prompt.setRole("user");
        if (prompt.getContent() == null || prompt.getContent().isEmpty()) {
            throw new IllegalArgumentException("Prompt content must not be null or empty");
        }
        return chatService.addPromptAndResponse(chat, prompt);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
