package com.example.backend.services;

import com.example.backend.models.Message;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.models.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final OpenAiService openAiService;

    public ChatService(ChatRepository chatRepository, OpenAiService openAiService) {
        this.chatRepository = chatRepository;
        this.openAiService = openAiService;
    }

    public List<Chat> getAllChats(String email) {
        return chatRepository.findAllByUserEmail(email);
    }


    public Message createChat(Chat newChat, String firstPrompt) {
        if (chatRepository.existsById(newChat.getId())) {
            throw new IllegalArgumentException("Chat with this ID already exists");
        }
        newChat.setMessages(new ArrayList<Message>());
        Message systemMessage = new Message("You are a helpful assistant. Be succinct - answer in 3-5 sentences.", "developer");
        newChat.addMessage(systemMessage);
        Message firstMessage = new Message(firstPrompt, "user");
        newChat.addMessage(firstMessage);
        Message response = openAiService.getResponse(newChat.getMessages());
        newChat.addMessage(response);
        Chat chat = chatRepository.save(newChat);
        chat.setTitle(openAiService.generateTitle(chat.getMessages()));
        chatRepository.save(chat);
        return response;
    }

    public Chat getChatById(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));
    }

    public Message addPromptAndResponse(Chat chat, Message prompt) {
        chat.addMessage(prompt);
        Message response = openAiService.getResponse(chat.getMessages());
        chat.addMessage(response);
        chatRepository.save(chat);
        return response;
    }

    // returns the chat if it belongs to the user, otherwise throws an exception
    public Chat getChatOrThrow(UUID chatId, String email) {
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
}
