package com.example.backend.services;

import com.example.backend.models.Message;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.util.List;


public class GoogleAiService {

    private final Client client;
    private final String model = "gemini-2.0-flash-001";

    //  gets the API key from the environment variable `GOOGLE_API_KEY`.
    public GoogleAiService(Client client) {
        this.client = client;
    }

    public Message getResponse(List<Message> messages) {
        // instruction goes separately from the chat history (in the json under the hood)
        Content instruction = Content.fromParts(Part.fromText("You are a helpful assistant. Be succinct - answer in 3-5 sentences."));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(instruction)
                .build();
        // Content is a class for messages in this SDK
        List<Content> history = messages.stream().map(m -> Content.builder()
                .role(m.getRole())
                .parts(Part.fromText(m.getContent()))
                .build()
        ).toList();
        // sends the whole convo and gets a response
        GenerateContentResponse response = client.models.generateContent(model, history, config);
        return new Message(response.text(), "model");
    }

    public String generateTitle(String firstPrompt) {
        Content instruction = Content.fromParts(Part.fromText("Generate a concise title for the user's message"));
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(instruction)
                .build();
        GenerateContentResponse response = client.models.generateContent(model, firstPrompt, config);
        return response.text();
    }
}


