package com.example.backend.services;

import com.example.backend.models.Message;
import com.example.backend.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.models.Chat;

import java.util.ArrayList;
import java.util.List;

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
}
