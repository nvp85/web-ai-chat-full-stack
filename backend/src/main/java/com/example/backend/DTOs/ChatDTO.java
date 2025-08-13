package com.example.backend.DTOs;

import com.example.backend.models.Chat;

import com.example.backend.models.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to receive a chat obj and a first prompt at once
// and to pass a chat obj and an LLM response at once
// usually only one last message is needed to be passed
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    private Chat chat;
    private Message message;
}
