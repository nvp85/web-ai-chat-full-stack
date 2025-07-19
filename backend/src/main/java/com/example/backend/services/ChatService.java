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
    private final GoogleAiService googleAiService;

    public ChatService(ChatRepository chatRepository, OpenAiService openAiService, GoogleAiService googleAiService) {
        this.chatRepository = chatRepository;
        this.openAiService = openAiService;
        this.googleAiService = googleAiService;
    }

    public List<Chat> getAllChats(String email) {
        return chatRepository.findAllByUserEmail(email);
    }

    // creates a new chat, adds the first prompt and response to it
    public Chat createChat(Chat newChat, String firstPrompt) {
        if (chatRepository.existsById(newChat.getId())) {
            throw new IllegalArgumentException("Chat with this ID already exists");
        }
        newChat.setMessages(new ArrayList<Message>());
        // the instruction was moved to the AI services
        Message firstMessage = new Message(firstPrompt, "user");
        newChat.addMessage(firstMessage);
        Message response = switch (newChat.getLlModel().getId()) {
            case 1 -> openAiService.getResponse(newChat.getMessages());
            case 2 -> googleAiService.getResponse(newChat.getMessages());
            default -> throw new IllegalArgumentException("Unknown LLM");
        };
        newChat.addMessage(response);
        Chat chat = chatRepository.save(newChat);
        String title = "Untitled";
        try {
            title = switch (newChat.getLlModel().getId()) {
                case 1 -> openAiService.generateTitle(firstPrompt);
                case 2 -> googleAiService.generateTitle(firstPrompt);
                default -> throw new IllegalArgumentException("Unknown LLM");
            };
        } catch (Exception e) {
            // title is optional
            // if this call fails the chat just remains untitled
        }
        chat.setTitle(title);
        chatRepository.save(chat);
        return chat;
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

    public void deleteChat(Chat chat) {
        chatRepository.deleteById(chat.getId());
    }
}
