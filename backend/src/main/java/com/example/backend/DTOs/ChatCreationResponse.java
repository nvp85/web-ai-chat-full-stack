package com.example.backend.DTOs;

import com.example.backend.models.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to pass a chat obj and an LLM response at once
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCreationResponse {
    private Chat chat;
    private String response;
}
