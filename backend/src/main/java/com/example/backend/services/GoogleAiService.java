package com.example.backend.services;

import com.example.backend.models.Message;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.util.List;


public class GoogleAiService {

    private final Client client = new Client();
    //  gets the API key from the environment variable `GOOGLE_API_KEY`.

    public Message getResponse(List<Message> messages) {

        Content instruction = Content.fromParts(Part.fromText("You are a helpful assistant. Be succinct - answer in 3-5 sentences."));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(instruction)
                .build();

        List<Content> history = messages.stream().map(m -> Content.builder()
                .role(m.getRole())
                .parts(Part.fromText(m.getContent()))
                .build()
        ).toList();

        Chat chat = client.chats.create("gemini-2.0-flash-001", config);
        GenerateContentResponse response = chat.sendMessage(history);
        return new Message(response.text(), "model");
    }


}


