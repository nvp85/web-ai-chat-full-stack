package com.example.backend.services;

import com.example.backend.DTOs.ChatDTO;
import com.example.backend.Events.MessagesCreatedEvent;
import com.example.backend.models.Chat;
import com.example.backend.models.LLModel;
import com.example.backend.models.Message;
import com.example.backend.models.User;
import com.example.backend.registries.LlmRegistry;
import com.example.backend.repositories.ChatRepository;
import com.example.backend.repositories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    ChatRepository chatRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    OpenAiService openAiService;

    @Mock
    GoogleAiService googleAiService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    ChatService chatService;

    LlmRegistry llmRegistry;

    @BeforeEach
    void setUp() {
        when(openAiService.provider()).thenReturn("OpenAI");
        when(googleAiService.provider()).thenReturn("Google");
        llmRegistry = new LlmRegistry(List.of(openAiService, googleAiService));
        chatService = new ChatService(chatRepository, messageRepository, llmRegistry, applicationEventPublisher);
    }

    @Test
    void createChatWithGPT() throws Exception {
        Chat newChat = new Chat();
        newChat.setId(java.util.UUID.randomUUID());
        newChat.setLlModel(new LLModel(1, "gpt-4o-mini", "OpenAI"));
        String firstPrompt = "Hello, world!";
        Message aiResponse = new Message("Hi there!", "assistant", null);
        when(chatRepository.existsById(newChat.getId())).thenReturn(false);
        when(openAiService.getResponse(anyList())).thenReturn(aiResponse);
        when(openAiService.generateTitle(firstPrompt)).thenReturn("Test Chat Title");
        when(chatRepository.save(newChat)).thenAnswer(inv -> inv.getArgument(0));
        ChatDTO dto = chatService.createChat(newChat, firstPrompt);
        // Verify the chat was saved
        verify(chatRepository).save(argThat(savedChat ->
                savedChat.getMessages().size() == 2 &&
                savedChat.getMessages().get(0).getContent().equals(firstPrompt) &&
                savedChat.getMessages().get(1).getContent().equals("Hi there!") &&
                savedChat.getTitle().equals("Test Chat Title")
        ));
        // Verify the event was published
        ArgumentCaptor<MessagesCreatedEvent> captor = ArgumentCaptor.forClass(MessagesCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
    }

    @Test
    void createChatWithGemini() throws Exception {
        Chat newChat = new Chat();
        newChat.setId(java.util.UUID.randomUUID());
        newChat.setLlModel(new LLModel(2, "gemini-flash", "Google"));
        String firstPrompt = "Hello, Gemini!";
        Message aiResponse = new Message("Hello, human!", "assistant", null);
        when(chatRepository.existsById(newChat.getId())).thenReturn(false);
        when(googleAiService.getResponse(anyList())).thenReturn(aiResponse);
        when(googleAiService.generateTitle(firstPrompt)).thenReturn("Gemini Chat Title");
        when(chatRepository.save(newChat)).thenAnswer(inv -> inv.getArgument(0));
        ChatDTO dto = chatService.createChat(newChat, firstPrompt);
        assertEquals("Hello, human!", dto.getMessage().getContent());
        // the google service was used, not openai
        verify(openAiService, never()).getResponse(anyList());
        verify(googleAiService).getResponse(anyList());
    }

    @Test
    void createChatAlreadyExists() {
        Chat newChat = new Chat();
        newChat.setId(java.util.UUID.randomUUID());
        String firstPrompt = "Hello, world!";
        when(chatRepository.existsById(newChat.getId())).thenReturn(true);
        assertThrows(Exception.class, () -> {chatService.createChat(newChat, firstPrompt);});
        verify(chatRepository, never()).save(any(Chat.class));
        verify(openAiService, never()).getResponse(anyList());
        verify(googleAiService, never()).getResponse(anyList());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void createChatUnknownLLM() {
        Chat newChat = new Chat();
        newChat.setId(java.util.UUID.randomUUID());
        newChat.setLlModel(new LLModel(99, "unknown-model", "Unknown"));
        String firstPrompt = "Hello, world!";
        when(chatRepository.existsById(newChat.getId())).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.createChat(newChat, firstPrompt);});
        assertTrue(exception.getMessage().contains("Unknown LLM provider"));
    }

    @Test
    void getChatById() throws Exception {
        Chat chat = new Chat();
        chat.setId(java.util.UUID.randomUUID());
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        Chat found = chatService.getChatById(chat.getId());
        assertEquals(chat.getId(), found.getId());
    }

    @Test
    void getChatByIdNotFound() {
        java.util.UUID chatId = java.util.UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(Exception.class, () -> {chatService.getChatById(chatId);});
        assertEquals("Chat not found with ID: " + chatId, exception.getMessage());
    }

    @Test
    void addPromptAndResponse() throws Exception {
        Chat chat = new Chat();
        chat.setId(java.util.UUID.randomUUID());
        chat.setMessages(new ArrayList<Message>());
        chat.setLlModel(new LLModel(1, "gpt-4o-mini", "OpenAI"));
        Message prompt = new Message("What's the weather?", "user", null);
        Message aiResponse = new Message("It's sunny!", "assistant", null);
        when(openAiService.getResponse(anyList())).thenReturn(aiResponse);
        when(chatRepository.save(chat)).thenAnswer(inv -> inv.getArgument(0));
        ChatDTO dto = chatService.addPromptAndResponse(chat, prompt);
        assertEquals("It's sunny!", dto.getMessage().getContent());
        verify(chatRepository).save(argThat(savedChat ->
                savedChat.getMessages().size() == 2 &&
                savedChat.getMessages().get(0).getContent().equals("What's the weather?") &&
                savedChat.getMessages().get(1).getContent().equals("It's sunny!")
        ));
        assertEquals(chat.getId(), dto.getChat().getId());
        verify(applicationEventPublisher).publishEvent(any(MessagesCreatedEvent.class));
    }

    @Test
    void getChatOrThrow() throws Exception {
        Chat chat = new Chat();
        User user = new User();
        chat.setId(java.util.UUID.randomUUID());
        user.setEmail("a@b.com");
        chat.setUser(user);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        Chat found = chatService.getChatOrThrow(chat.getId(), "a@b.com");
        assertEquals(chat.getId(), found.getId());
    }

    @Test
    void getChatOrThrowAccessDenied() {
        Chat chat = new Chat();
        User user = new User();
        chat.setId(java.util.UUID.randomUUID());
        user.setEmail("a@b.com");
        chat.setUser(user);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        Exception exception = assertThrows(Exception.class, () -> {
            chatService.getChatOrThrow(chat.getId(), "c@d.com");
        });
        assertEquals("You do not have permission to access this chat", exception.getMessage());
    }

    @Test
    void getChatOrThrowNotFound() {
        java.util.UUID chatId = java.util.UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(Exception.class, () -> {
            chatService.getChatOrThrow(chatId, "a@b.com");
        });
        assertEquals("Chat not found with ID: " + chatId, exception.getMessage());
    }

    @Test
    void updateChatTitle() {
        Chat chat = new Chat();
        chat.setId(java.util.UUID.randomUUID());
        chat.setTitle("Old Title");
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
        Chat updated = chatService.updateChatTitle(chat, "New Title");
        assertEquals("New Title", updated.getTitle());
        verify(chatRepository).save(argThat(savedChat -> savedChat.getTitle().equals("New Title")));
    }

    @Test
    void deleteChat() {
        Chat chat = new Chat();
        chat.setId(java.util.UUID.randomUUID());
        doNothing().when(chatRepository).deleteById(chat.getId());
        chatService.deleteChat(chat);
        verify(chatRepository).deleteById(chat.getId());
    }
}