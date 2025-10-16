package com.example.backend.services;

import com.example.backend.models.Message;

import java.util.List;

public interface LlmService {
    String provider();
    Message getResponse(List<Message> messages);
    String generateTitle(String firstPrompt);
}
