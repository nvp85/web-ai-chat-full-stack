package com.example.backend.services;

import com.example.backend.exceptions.ChatAlreadyExistsException;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.LLModel;
import com.example.backend.models.Message;
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

// This service contains the business logic for working with chats
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final OpenAiService openAiService;
    private final GoogleAiService googleAiService;

    public ChatService(ChatRepository chatRepository, OpenAiService openAiService, GoogleAiService googleAiService) {
        this.chatRepository = chatRepository;
        this.openAiService = openAiService;
        this.googleAiService = googleAiService;
    }
    // returns all user's chats by user's email
    public List<Chat> getAllChats(String email) {
        return chatRepository.findAllByUserEmail(email);
    }

    // The method creates a new chat, adds the first prompt and response to it.
    // It is one transaction with the isolation lvl serializable
    // because otherwise it was possible to have several chat creation requests with the same id
    // one after another and the last one got actually saved in the DB.
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Chat createChat(Chat newChat, String firstPrompt) throws ChatAlreadyExistsException {
        if (chatRepository.existsById(newChat.getId())) {
            throw new ChatAlreadyExistsException();
        }
        newChat.setMessages(new ArrayList<Message>());
        Message firstMessage = new Message(firstPrompt, "user");
        newChat.addMessage(firstMessage);
        Message response = switch (newChat.getLlModel().getId()) {
            case 1 -> openAiService.getResponse(newChat.getMessages());
            case 2 -> googleAiService.getResponse(newChat.getMessages());
            default -> throw new IllegalArgumentException("Unknown LLM");
        };
        newChat.addMessage(response);
        Chat chat = chatRepository.save(newChat);
        String title = generateChatTitle(firstPrompt, chat.getLlModel());
        chat.setTitle(title);
        chatRepository.save(chat);
        return chat;
    }

    public Chat getChatById(UUID chatId) throws NotFoundException{
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found with ID: " + chatId));
    }

    // sends a prompt, gets a response and saves both into the DB
    public Message addPromptAndResponse(Chat chat, Message prompt) {
        chat.addMessage(prompt);
        Message response = switch (chat.getLlModel().getId()) {
            case 1 -> openAiService.getResponse(chat.getMessages());
            case 2 -> googleAiService.getResponse(chat.getMessages());
            default -> throw new IllegalArgumentException("Unknown LLM");
        };
        chat.addMessage(response);
        chatRepository.save(chat);
        return response;
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
