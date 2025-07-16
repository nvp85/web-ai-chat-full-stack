package com.example.backend.services;

import com.example.backend.models.Message;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class OpenAiService {
    private final OpenAIClient openAIClient;
    private final ChatModel chatModel = ChatModel.GPT_4O_MINI;

    public OpenAiService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public Message getResponse(List<Message> messages) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder().model(chatModel);
        for (Message msg : messages) {
            // enhanced switch to handle different roles
            switch (msg.getRole()) {
                case "developer"    -> b.addDeveloperMessage(msg.getContent());
                case "assistant" -> b.addAssistantMessage(msg.getContent());
                case "user"      -> b.addUserMessage(msg.getContent());
                default          -> throw new IllegalArgumentException("Unknown role: " + msg.getRole());
            }
        }
        ChatCompletionCreateParams params = b.build();
        String response = openAIClient.chat().completions().create(params).choices().get(0).message().content().get();
        return new Message(response, "assistant", null);
    }
}
