package com.example.backend.services;

import com.example.backend.exceptions.ChatAlreadyExistsException;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.Message;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.models.Chat;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public Chat getChatById(UUID chatId) throws NotFoundException{
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found with ID: " + chatId));
    }

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
}
