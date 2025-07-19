package com.example.backend.DTOs;

import com.example.backend.models.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCreationResponse {
    private Chat chat;
    private String response;
}
