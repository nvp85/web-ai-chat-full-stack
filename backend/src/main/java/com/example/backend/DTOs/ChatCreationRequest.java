package com.example.backend.DTOs;

import com.example.backend.models.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to receive a chat obj and a first prompt at once
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCreationRequest {
    private Chat chat;
    private String firstPrompt;
}
