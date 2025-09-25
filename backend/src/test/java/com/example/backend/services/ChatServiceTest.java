package com.example.backend.services;

import com.example.backend.DTOs.ChatDTO;
import com.example.backend.Events.MessagesCreatedEvent;
import com.example.backend.models.Chat;
import com.example.backend.models.LLModel;
import com.example.backend.models.Message;
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

    @InjectMocks
    ChatService chatService;

    @BeforeEach
    void setUp() {
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
    void getChatById() {
    }

    @Test
    void addPromptAndResponse() {
    }

    @Test
    void getChatOrThrow() {
    }

    @Test
    void updateChatTitle() {
    }

    @Test
    void deleteChat() {
    }
}