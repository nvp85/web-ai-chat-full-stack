package com.example.backend.services;

import com.example.backend.DTOs.ChatDTO;
import com.example.backend.Events.MessagesCreatedEvent;
import com.example.backend.exceptions.ChatAlreadyExistsException;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.LLModel;
import com.example.backend.models.Message;
import com.example.backend.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.models.Chat;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// This service contains the business logic for working with chats
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final OpenAiService openAiService;
    private final GoogleAiService googleAiService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // returns all user's chats by user's email
    public List<Chat> getAllChats(String email) {
        return chatRepository.findAllByUserEmail(email);
    }

    // The method creates a new chat, adds the first prompt and response to it.
    @Transactional
    public ChatDTO createChat(Chat newChat, String firstPrompt) throws ChatAlreadyExistsException, ExecutionException, InterruptedException {
        if (chatRepository.existsById(newChat.getId())) {
            throw new ChatAlreadyExistsException();
        }
        newChat.setMessages(new ArrayList<Message>());
        Message firstMessage = new Message(firstPrompt, "user");
        newChat.addMessage(firstMessage);
        CompletableFuture<Message> response = CompletableFuture.supplyAsync(() -> switch (newChat.getLlModel().getId()) {
            case 1 -> openAiService.getResponse(newChat.getMessages());
            case 2 -> googleAiService.getResponse(newChat.getMessages());
            default -> throw new IllegalArgumentException("Unknown LLM");
        });
        CompletableFuture<String> title = CompletableFuture.supplyAsync(
                () -> generateChatTitle(firstPrompt, newChat.getLlModel()));
        CompletableFuture<Void> both = CompletableFuture.allOf(response, title);
        both.join();
        newChat.addMessage(response.get());
        newChat.setTitle(title.get());
        Chat chat = chatRepository.save(newChat);
        applicationEventPublisher.publishEvent(new MessagesCreatedEvent(this, chat.getMessages()));
        return new ChatDTO(chat, response.get());
    }

    public Chat getChatById(UUID chatId) throws NotFoundException{
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found with ID: " + chatId));
    }

    // sends a prompt, gets a response and saves both into the DB
    @Transactional
    public ChatDTO addPromptAndResponse(Chat chat, Message prompt) {
        chat.addMessage(prompt);
        Message response = switch (chat.getLlModel().getId()) {
            case 1 -> openAiService.getResponse(chat.getMessages());
            case 2 -> googleAiService.getResponse(chat.getMessages());
            default -> throw new IllegalArgumentException("Unknown LLM");
        };
        chat.addMessage(response);
        Chat savedChat = chatRepository.save(chat);
        // get the persisted messages
        List<Message> lastTwoMessages = messageRepository.findByChatOrderByIdDesc(chat, Limit.of(2));
        applicationEventPublisher.publishEvent(new MessagesCreatedEvent(this, lastTwoMessages));
        return new ChatDTO(savedChat, response);
    }

    // returns the chat if it belongs to the user, otherwise throws an exception
    public Chat getChatOrThrow(UUID chatId, String email) throws NotFoundException {
        Chat chat = getChatById(chatId);
        if (!chat.getUser().getEmail().equals(email)) {
            // Spring security will handle this exception
            throw new AccessDeniedException("You do not have permission to access this chat");
        }
        return chat;
    }

    public Chat updateChatTitle(Chat chat, String newTitle) {
        chat.setTitle(newTitle);
        return chatRepository.save(chat);
    }

    public void deleteChat(Chat chat) {
        chatRepository.deleteById(chat.getId());
    }

    // Attempts to generate a title with an LLM
    // if the call fails returns first 5 words of the first prompt as a title
    private String generateChatTitle(String firstPrompt, LLModel llm) {
        String title = "Untitled";
        try {
            title = switch (llm.getId()) {
                case 1 -> openAiService.generateTitle(firstPrompt);
                case 2 -> googleAiService.generateTitle(firstPrompt);
                default -> throw new IllegalArgumentException("Unknown LLM");
            };
        } catch (Exception e) {
            String[] words = firstPrompt.trim().split("\\s+");
            // takes 5 first words as a title
            title = String.join(" ", Arrays.copyOf(words, Math.min(words.length, 5)));
            title = words.length > 5 ? title + "..." : title;
        }
        return title;
    }
}
