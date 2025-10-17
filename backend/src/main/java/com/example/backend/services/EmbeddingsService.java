package com.example.backend.services;

import com.example.backend.models.Message;

import java.util.List;

public interface EmbeddingsService {
    List<float[]> embed(List<Message> messages);
}
