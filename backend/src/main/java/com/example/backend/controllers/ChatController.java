package com.example.backend.controllers;

import com.example.backend.DTOs.ChatCreationRequest;
import com.example.backend.DTOs.ChatCreationResponse;
import com.example.backend.exceptions.ChatAlreadyExistsException;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.*;
import com.example.backend.services.ChatService;
import com.example.backend.services.UserService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // Get all the chat that belong to the current user
    @GetMapping
    public List<Chat> getAllChats(@AuthenticationPrincipal JwtUser jwtUser) {
        // jwtUser username is the email of the user
        return chatService.getAllChats(jwtUser.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatCreationResponse createChat(
            @AuthenticationPrincipal JwtUser jwtUser,
            @RequestBody ChatCreationRequest chatCreationDTO) throws ChatAlreadyExistsException {
        // ChatCreationDTO contains the chat object and the first prompt
        User user = userService.getUserByEmail(jwtUser.getUsername());
        Chat newChat = chatCreationDTO.getChat();
        newChat.setUser(user);
        Chat chat = chatService.createChat(newChat, chatCreationDTO.getFirstPrompt());
        return new ChatCreationResponse(chat, chat.getMessages().getLast().getContent());
    }

    // Get chat by id without its messages
    @GetMapping("/{chatId}")
    public Chat getChatById(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        // validate ownership: getChatOrThrow will return the chat only if it belongs to the curr user
        return chatService.getChatOrThrow(chatId, jwtUser.getUsername());
    }

    // I would use PATCH here but the project requirements say PUT
    @PutMapping("/{chatId}")
    public Chat updateChatTitle(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId, @RequestBody Chat newTitle) throws NotFoundException {
        // we need only the title from the request body Chat object
        // getChatOrThrow will return the chat only if it belongs to the curr user
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        return chatService.updateChatTitle(chat, newTitle.getTitle()); // return updated chat
    }

    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        chatService.deleteChat(chat);
    }

    @GetMapping("/{chatId}/messages")
    public List<Message> getChatMessages(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        // getChatOrThrow will return the chat only if it belongs to the curr user
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        return chat.getMessages();
    }

    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message promptAndGetResponse(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId, @RequestBody Message prompt) throws NotFoundException {
        // validate ownership
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleIllegalArgumentException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
