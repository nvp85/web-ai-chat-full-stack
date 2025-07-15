package com.example.backend.services;

import org.springframework.stereotype.Service;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.models.Chat;

import java.util.List;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<Chat> getAllChats(String email) {
        return chatRepository.findAllByUserEmail(email);
    }
}
