package com.example.backend.controllers;

import com.example.backend.DTOs.ChatDTO;
import com.example.backend.exceptions.ChatAlreadyExistsException;
import com.example.backend.exceptions.EmailAlreadyExistsException;
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

    // GET all chats that belong to the current user
    // Endpoint http://localhost:8080/api/chats
    @GetMapping
    public List<Chat> getAllChats(@AuthenticationPrincipal JwtUser jwtUser) {
        // jwtUser username is the email of the user
        return chatService.getAllChats(jwtUser.getUsername());
    }

    // POST a new chat from the request body chat creation obj
    // Endpoint http://localhost:8080/api/chats
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDTO createChat(
            @AuthenticationPrincipal JwtUser jwtUser,
            @RequestBody ChatDTO chatCreationDTO) throws ChatAlreadyExistsException, NotFoundException {
        // ChatCreationDTO contains the chat object and the first prompt
        if (chatCreationDTO.getMessage() == null || chatCreationDTO.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Prompt content must not be null or empty");
        }
        User user = userService.getUserByEmail(jwtUser.getUsername());
        Chat newChat = chatCreationDTO.getChat();
        newChat.setUser(user);
        Chat chat = chatService.createChat(newChat, chatCreationDTO.getMessage());
        return new ChatDTO(chat, chat.getMessages().getLast().getContent());
    }

    // GET a chat by id without its messages
    // Endpoint example: http://localhost:8080/api/chats/fa1b85c1-8216-418e-8a53-5014ba3b3aa6
    @GetMapping("/{chatId}")
    public Chat getChatById(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        // validate ownership: getChatOrThrow will return the chat only if it belongs to the curr user
        return chatService.getChatOrThrow(chatId, jwtUser.getUsername());
    }

    // PUT (update) a chat's title
    // Endpoint example: http://localhost:8080/api/chats/fa1b85c1-8216-418e-8a53-5014ba3b3aa6
    @PutMapping("/{chatId}")
    public Chat updateChatTitle(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId, @RequestBody Chat newTitle) throws NotFoundException {
        // we need only the title from the request body Chat object
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        return chatService.updateChatTitle(chat, newTitle.getTitle()); // return updated chat
    }

    // DELETE a chat by id
    // Endpoint example: http://localhost:8080/api/chats/fa1b85c1-8216-418e-8a53-5014ba3b3aa6
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        chatService.deleteChat(chat);
    }

    // GET a chat's messages by id
    // Endpoint example: http://localhost:8080/api/chats/fa1b85c1-8216-418e-8a53-5014ba3b3aa6/messages
    @GetMapping("/{chatId}/messages")
    public List<Message> getChatMessages(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId) throws NotFoundException {
        // getChatOrThrow will return the chat only if it belongs to the curr user
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        return chat.getMessages();
    }

    // POST a message to a chat
    // Endpoint example: http://localhost:8080/api/chats/fa1b85c1-8216-418e-8a53-5014ba3b3aa6/messages
    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDTO promptAndGetResponse(
            @AuthenticationPrincipal JwtUser jwtUser,
            @PathVariable UUID chatId, @RequestBody Message prompt) throws NotFoundException {
        Chat chat = chatService.getChatOrThrow(chatId, jwtUser.getUsername());
        prompt.setRole("user");
        if (prompt.getContent() == null || prompt.getContent().isEmpty()) {
            throw new IllegalArgumentException("Prompt content must not be null or empty");
        }
        return chatService.addPromptAndResponse(chat, prompt);
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<String> handleChatAlreadyExistsException(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
