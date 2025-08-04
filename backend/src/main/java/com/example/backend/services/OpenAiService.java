package com.example.backend.services;

import com.example.backend.models.Message;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.stereotype.Service;
import java.util.List;

// This service is responsible for interaction with GPT via OpenAI SDK
@Service
public class OpenAiService {
    private final OpenAIClient openAIClient;
    private final ChatModel chatModel = ChatModel.GPT_4O_MINI;

    public OpenAiService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    // a helper method to create a builder of a chat history (it contains all the messages)
    private ChatCompletionCreateParams.Builder createParamsBuilder(List<Message> messages, String instruction) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder().model(chatModel);
        b.addSystemMessage(instruction);
        for (Message msg : messages) {
            // enhanced switch to handle different roles
            switch (msg.getRole()) {
                case "developer"    -> b.addDeveloperMessage(msg.getContent());
                case "assistant" -> b.addAssistantMessage(msg.getContent());
                case "user"      -> b.addUserMessage(msg.getContent());
                default          -> throw new IllegalArgumentException("Unknown role: " + msg.getRole());
            }
        }
        return b; // return the builder to add more messages if needed
    }

    public Message getResponse(List<Message> messages) {
        String instruction = "You are a helpful assistant. Be succinct - answer in 3-5 sentences.";
        ChatCompletionCreateParams params = createParamsBuilder(messages, instruction).build();
        // sends a convo and gets a response
        String response = openAIClient.chat().completions().create(params).choices().get(0).message().content().get();
        return new Message(response, "assistant", null);
    }

    public String generateTitle(String firstPrompt) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder().model(chatModel);
        b.addSystemMessage("Generate a concise title for the chat based on the user's messages.");
        b.addUserMessage(firstPrompt);
        ChatCompletionCreateParams params = b.build();
        String response = openAIClient.chat().completions().create(params).choices().get(0).message().content().get();
        return response;
    }
}
