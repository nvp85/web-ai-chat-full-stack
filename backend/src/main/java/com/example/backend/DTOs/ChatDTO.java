package com.example.backend.DTOs;

import com.example.backend.models.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to receive a chat obj and a first prompt at once
// and to pass a chat obj and an LLM response at once
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    private Chat chat;
    private String message;
}
