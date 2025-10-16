package com.example.backend.services;

import com.example.backend.models.Message;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

// This service is responsible for interaction with GPT via OpenAI SDK
@Service
public class OpenAiService implements LlmService, EmbeddingsService {
    private final OpenAIClient openAIClient;
    private final ChatModel chatModel = ChatModel.GPT_4O_MINI;
    private final EmbeddingModel embeddingModel = EmbeddingModel.TEXT_EMBEDDING_3_SMALL;

    public OpenAiService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    public String provider() {
        return "OpenAI";
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

    @Override
    public Message getResponse(List<Message> messages) {
        String instruction = "You are a helpful assistant. Be succinct - answer in 3-5 sentences.";
        ChatCompletionCreateParams params = createParamsBuilder(messages, instruction).build();
        // sends a convo and gets a response
        String response = openAIClient.chat().completions().create(params).choices().get(0).message().content().get();
        return new Message(response, "assistant", null);
    }

    @Override
    public String generateTitle(String firstPrompt) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder().model(chatModel);
        b.addSystemMessage("Generate a concise title for the chat based on the user's messages. Respond only with the title and nothing else.");
        b.addUserMessage(firstPrompt);
        ChatCompletionCreateParams params = b.build();
        String response = openAIClient.chat().completions().create(params).choices().get(0).message().content().get();
        return response;
    }

    @Override
    public List<float[]> embed(List<Message> messages) {
        EmbeddingCreateParams.Builder b = EmbeddingCreateParams.builder().model(embeddingModel);
        b.inputOfArrayOfStrings(messages.stream().map(Message::getContent).toList());
        b.dimensions(768);
        EmbeddingCreateParams params = b.build();
        List<Embedding> response = openAIClient.embeddings().create(params).data();
        return response.stream().map(em -> {
                    List<Float> embeddings = em.embedding();
                    float[] arr = new float[embeddings.size()];
                    for (int i=0; i < embeddings.size(); i++) {
                        arr[i] = embeddings.get(i);
                    }
                    return  arr;
                }
                ).toList();
    }
}
